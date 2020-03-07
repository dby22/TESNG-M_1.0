import produce_aim_Q
import gFunction
import random
import igraph
import changeNode
import changeEdge


if __name__ == '__main__':
    '''
    :param
    :function Dynamic social network generator
    '''
    print('='*8,'Select original network','='*8)
    print('{0:1} {1:15}'.format('','1、football'))
    print('{0:1} {1:15}'.format('','2、polbooks'))
    while 1:
        print('The selected original network：')
        numberOfdtset = input()
        if int(numberOfdtset) == 1:
            break
        elif int(numberOfdtset) == 2:
            break
        else:
            print('Incorrect input, please input again')

    sorFilename = {'1':'../data/football.gml','2':'../data/polbooks.gml'}
    desFilename = {'1':'../generate_graph/football/football','2':'../generate_graph/polbooks/polbooks'}

    granphNumber = 10
    T = 20
    p_exchange = 0.6
    Q_list = list()
    aim_Q_list = produce_aim_Q.produce_aim_Q(granphNumber)
    random.seed(1)

    # The generation
    for number in range(granphNumber):
        if number == 0:
            filename = sorFilename[str(numberOfdtset)]
        else:
            filename = desFilename[str(numberOfdtset)] + str(number) + '.gml'

        # Read file, compute original modularity
        G = igraph.Graph.Read_GML(filename)
        Q = gFunction.computQ(G)

        # Expected modularity
        aim_Q = aim_Q_list[number]

        if abs(aim_Q - Q) < 0.001:
            break

        # Evolution
        else:
            # NodeEvolution
            G = changeNode.change(G, p_exchange)
            Q = gFunction.computQ(G)

            if abs(aim_Q - Q) < 0.001:
                break

            # EdgeEvolution
            else:
                G = changeEdge.change(G, Q, aim_Q, T)
                Q = gFunction.computQ(G)
        G.write_gml(desFilename[str(numberOfdtset)] + str(number + 1) + '.gml')
        Q_list.append(Q)
    gFunction.writeQtoTXT(Q_list)
