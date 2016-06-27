# -*- coding: utf-8 -*-
"""
Created on Sun May 29 15:43:47 2016

@author: krikunov
"""

import math
import datetime
import numpy as np
import scipy.stats as stats

from utils import timedelta_minutes

def _gen_sistolic_by_age(age):
    """ Generates sistolic pressure specific for the given age.
        Uses conditional exponential weibull distribution with pre-defined params.
        
        Parameters
        ----------
        age : int, between 10 and 90 years
            Age of the person
        
        Returns
        ----------
        sistolic_pressure : int 
            Sistolic pressure
        
        """        
    #HARCODED! params are precalculated 
    if age <= 50:
        k = -51.43 + np.exp(0.04 * age + 3.64)
        l = 0.49 + np.exp(-0.03 * age + 1.21)
        a = -54.24 + np.exp(-0.01 * age + 5.11)
    else:
        k = -2.23 + np.exp(-0.06 * age + 8.53)
        l = 1.07 + 0.02 * np.exp(0.04 * age)
        a = 16.00 + 3.19 * np.exp(0.03 * age)
    return stats.exponweib.rvs(k, l, 0, a)

def _generate_sleep_time():
    """Generates time-to-sleep where sleeptime begining normally distributed as N(23, 0.5) 
       and end distributes as the N(7, 0.5)"""   
    t0 = 24
    while(t0 > 23.99):
        t0 = np.random.normal(23, .5)
    t0_hour, t0_minutes = int(t0), int((t0 % 1) * 60)
    
    t1 = np.random.normal(7, 0.5)
    t1h, t1m = int(t1), int((t1 % 1) * 60)

    t0_dt = datetime.time(t0_hour, t0_minutes)
    t1_dt = datetime.time(t1h, t1m)
    
    return t0_dt, t1_dt


class Patient(object):      
    """
        Parameters
        ----------
        sex : string, 
            patiens gender, 'male' or 'female'
        age : int
            patients age, should be value between 10 and 90
        nf_type : string, defalut None
            night pressure fall type.
    """

    #HARCODED! table values
    _s_std = 15
    _day_d_std = 14
    _night_d_std = 12

    def __init__(self, sex, age, generator, npf_type = None):
        self.sex = sex
        self.age = age
        self.npf_type = npf_type
        self._mean_sistolic_pressure = np.random.normal(120, 3)
        self._mean_diastolic_pressure = np.random.normal(70, 3)
        
        self._sleep_time_beginning, self._sleep_time_end = _generate_sleep_time() 
        self._sleep_time = timedelta_minutes(self._sleep_time_beginning, self._sleep_time_end)
        
        lower, upper = generator[npf_type]
        self.NSAPD = np.random.uniform(lower, upper) * self._mean_sistolic_pressure / 100
        self.NDAPD = np.random.uniform(lower, upper) * self._mean_diastolic_pressure / 100
        
        
    @property
    def sleep_time(self):
        """ Patient's sleep time beginning and end """
        return self._sleep_time_beginning, self._sleep_time_end
        
    @property
    def mean_pressure(self):
        return self._mean_sistolic_pressure, self._mean_diastolic_pressure

    def gen_night_fall(self, time):
        """
        Generates pressure changes relatively to daiky mean in accordace with circadian rhythm.
        
        Parameters
        ----------
        time : datetime  
            time       
            
        Returns
        ----------
        s_var : int 
            sistolic pressure change
        d_var : int
            diastolic pressure change
        """
        if self._sleep_time_beginning <= time.time() or \
            self._sleep_time_end >= time.time():
            
            sleep_length = timedelta_minutes(self._sleep_time_beginning, time.time())
            s = - math.sin(math.pi * sleep_length / self._sleep_time)
            
            return s * self.NSAPD, s *self.NDAPD
             
        return 0, 0

    def gen_variability(self, time):
        """
        Generates patient pressure varability at the given time as bivariate gaussian vector.
        Covariation matrix for gaussian distribution calculates using correlation 0.7 and
        STD for both components dependends on time (STD in sleep and day time may vary).
        
        Parameters
        ----------
        time : datetime  
            time       
            
        Returns
        ----------
        s_var : int 
            sistolic pressure variation
        d_var : int
            diastolic pressure variation
        """
    
        #HARCODED! table values
        s_std = Patient._s_std
        if self._sleep_time_beginning <= time.time() or \
           self._sleep_time_end >= time.time():   
            d_std = Patient._night_d_std
        else:
            d_std = Patient._day_d_std
        
        # cov(a, b) = corr(a,b) * std(a) * std(b)
        cov = 0.7 * s_std * d_std
        cov_matrix = [[s_std, cov],
                      [cov, d_std]]
                      
        return np.random.multivariate_normal([0, 0], cov_matrix)
                      

    def gen_pressure_change_event(self):
        """
        Generates event connected with the increase in arterial pressure for the current patient.
            
        Returns
        ----------
        sp_change : int
            Value of sistolic pressure after event
        dp_change : int
            Value of diastolic pressure after event
        """    
    
    #    if end <= begin:
    #        raise Error
        
        """Generates sistolic pressure conditional on the age of the person. """
        sp_change = -1    
        while sp_change < 130:
            sp_change = _gen_sistolic_by_age(self.age)
        
        """Then generates diastolic pressure normally distributed with parameters
           conditional on sistolic pressure value.""" 
        #HARCODED! params are precalculated
        dp_mu = 34.29 + 0.36 * sp_change
        dp_sigma = - 4.34 + 0.1 * sp_change
        dp_change = -1
        while dp_change < 90:
            dp_change = np.random.normal(dp_mu, dp_sigma)
    
        return sp_change - self._mean_sistolic_pressure, dp_change - self._mean_diastolic_pressure
    