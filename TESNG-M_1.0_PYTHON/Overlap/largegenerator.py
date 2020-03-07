import igraph
import produce_aim_Q
import random
import largeGenerator.changepro
import time


if __name__ == '__main__':
    filename = '../data/dblp.gml'

    granphNumber = 10
    T = 20
    p_exchange = 0.6
    Q_list = list()
    aim_Q_list = produce_aim_Q.produce_aim_Q(granphNumber)
    random.seed(1)

    for number in range(granphNumber):
        if number == 0:
            filename = '../data/dblp.gml'
            # Read the file and compute the original modularity
            G = igraph.Graph.Read_GML(filename)
            Q = largeGenerator.changepro.computQ(G)[0]
            Q_list.append(Q)
        else:
            filename = '../data/dblp'+str(number) + '.gml'
            G = igraph.Graph.Read_GML(filename)
            Q = Q_list[number-1]

        # Get the expected modularity
        aim_Q = aim_Q_list[number]

        if abs(aim_Q - Q) < 0.001:
            break

        # The generation
        else:

            # NodeEvolution
            G = largeGenerator.changepro.changenode(G, p_exchange)
            Q,detail_Q = largeGenerator.changepro.computQ(G)

            if abs(aim_Q - Q) < 0.001:
                break

            # EdgeEvolution
            else:
                G,Q,detail_Q= largeGenerator.changepro.changedge(G, Q, aim_Q, T, detail_Q)
        Q_list.append(Q)
        G.write_gml('../generate_graph/dblp'+ str(number + 1) + '.gml')
    pass