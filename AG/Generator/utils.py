# -*- coding: utf-8 -*-
"""
Created on Sun May 29 15:47:45 2016

@author: krikunov
"""

def timedelta_minutes(t1, t2):
    """
    Calculates time delta betwee two datetime.time instances in minutes.
    If t1 > t2 then assumes that t1 is the previous day time instance.   
    """
    if t1 > t2:
        td_h = 24 - t1.hour + t2.hour
        td_m = 0 - t1.minute + t2.minute
    else:
        td_h = t2.hour - t1.hour
        td_m = t2.minute - t1.minute
        
    return td_h * 60 + td_m