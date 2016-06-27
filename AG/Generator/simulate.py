# -*- coding: utf-8 -*-
"""
Created on Wed May 25 16:03:19 2016

@author: krikunov
"""
import configparser
import bisect
import itertools
import sys
import os
import math
import logging

import numpy as np 
from datetime import datetime, timedelta

from patient import Patient

#For debug purposes
import matplotlib.pyplot as plt
DEBUG = False

def event_curve(time, event_time, step, pressure_increasing):
    sistolic_increasing, diastolic_increasing = pressure_increasing
    
    if time >= event_time:
        x = (time - event_time).total_seconds() // (60 * step)
        
        #TODO: random event length
        a = np.exp( -(5*(10**-3)) * x)
        if a > 0.001:
            return a*sistolic_increasing, a*diastolic_increasing
    return 0, 0
    
def events_changes(patient, timesteps, days, step, inv_event_freq):
    events_cnt = days // inv_event_freq if inv_event_freq > 0 else 0
    if events_cnt == 0:
        return (0, 0)
    curves = []
    for e in range(events_cnt):
        max_delta = (timesteps[-1] - timesteps[0]).total_seconds()
        td = np.random.uniform(0, max_delta)
        event_time = timesteps[0] + timedelta(seconds = td)
        
        pressure_increasing = patient.gen_pressure_change_event()
        e = lambda x: event_curve(x, event_time, step, pressure_increasing)            
        
        event = np.array(list(map(e, timesteps)))
        curves.append(event)
        
    return sum(curves)

def age_generator(sex = 'male', pool_size = 1000):   
    if sex == 'male':
        ages_pool = np.concatenate((np.random.normal(59.5, 10.66, int(pool_size * .73)),
                                np.random.normal(36.56, 9.95, int(pool_size * .22)),
                                np.random.normal(36.56, 9.95, int(pool_size * .5))
                                ))
    else:
        ages_pool = np.concatenate((np.random.normal(41.06, 10.25, int(pool_size * .12)),
                            np.random.normal(72.17, 6.19, int(pool_size * .36)),
                            np.random.normal(58.73, 6.36, int(pool_size * .53))
                            ))
    while True:
        yield np.random.choice(ages_pool)
    
def generate_patients(male_proportion, types, type_probs, npf, count = None):          
    c = itertools.count()
    age_generators = {'male': age_generator('male'), 'female': age_generator('female')}
    
    while True:
        pateint_type = types[bisect.bisect(type_probs, np.random.uniform())]
        sex = 'male' if np.random.uniform() <= male_proportion else 'female'
        age = next(age_generators[sex])        
        
        yield Patient(sex, age, npf, pateint_type)
        
        if count and next(c) >= count:
            print(count)
            break
    

if __name__ == '__main__':
    output = sys.argv[1]
    output_path = sys.argv[2]
    
    logging.basicConfig(level = logging.INFO)
    np.random.seed(236185)    
    
    
    if not os.path.exists(output_path):
        logging.info("Output directory does not exists. Creating path...")
        os.makedirs(output_path)
    
    c = configparser.ConfigParser()
    c.read('config.ini')     
       
    #Read current run configuration
    run = c['RUN']
    days = int(run.get('days'))
    step = int(run.get('step'))
    count = int(run.get('patients', 0))
    data_size = math.floor(float(run.get('data_size', 0)) * 1024 * 1024)
    chunk_size = math.floor(float(run.get('chunk_size', 0)) * 1024 * 1024)
    
    first_day_str = run.get('firstday', "01.01.2010 12:00")  
    first_day = datetime.strptime(first_day_str, "%d.%m.%Y %H:%M")
    
    #Read population configuration
    inv_event_freq = int(c['POPULATION'].get('inv_event_freq', 7))
    male_proportion = float(c['POPULATION'].get('male', .5))
    time_steps = [first_day + timedelta(minutes = x * step) for x in range(0, days * 24 * 60 // step)]    
    
    #Read configuration for each night pressure fall (NPF) type of patients
    types = [t for t in c.sections() if t not in ['RUN', 'POPULATION']]
    prevalences = [int(c[t]['prevalence']) for t in types]
    type_probs = np.cumsum(list(map(lambda x: x / 100, prevalences)))
    npf = { t:(int(c[t]['l']), int(c[t]['u'])) for t in types}   
    
    files = math.ceil(data_size / chunk_size) if chunk_size > 0 and not count else 1
    
    total_size = 0
    logging.info("Prepearing to write %s files", files)
    for chunk in range(files):
        size_left = data_size - total_size
        max_size = data_size if files == 1 else (size_left if size_left < chunk_size else chunk_size)
        
        fsize = 0
        filename = output if files == 1 else output + ".ch{}".format(chunk)
        with open(os.path.join(output_path, filename), 'w+') as output_file:
            for patient in generate_patients(male_proportion, types, type_probs, npf, count):
                patient_ap_variations = lambda t: patient.gen_night_fall(t) + patient.gen_variability(t)
                variations = np.array(list(map(patient_ap_variations, time_steps)))
                events = events_changes(patient, time_steps, days, step, inv_event_freq)
                
                result = variations + patient.mean_pressure + events
                
                header = "PATIENT {} {:.0f} {}\n".format(patient.sex, patient.age, patient.npf_type)
                data = header + ''.join("{};{:.2f};{:.2f}\n".format(time, *p) for time, p in zip(time_steps, result))
                
                if DEBUG:
                    print(header)
                    plt.plot(time_steps, result)  
                    plt.show()
                else:
                    if not count and fsize + sys.getsizeof(data) > max_size:
                        break
                    fsize += output_file.write(data)
        total_size += fsize
            