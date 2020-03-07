# author：byduan
# function： produce expected modularity
import random
def produce_aim_Q(N):
    random.seed(0)
    aim_Q_list = list()
    for i in range(N):
        while 1:
            aim_Q = random.random()
            if aim_Q >= 0.3 and aim_Q <= 0.7:
                break
        aim_Q_list.append(aim_Q)
    return aim_Q_list

