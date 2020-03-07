import gFunction
import random
import  math

def change(G,Q,aim_Q,T):
    for t in range(100000):
        if abs(aim_Q - Q) < 0.001:
            break
        else:
            temp = gFunction.judge(G)
            addOrDelete = temp[0]
            randomSelectedNode = temp[1]
            detail_infor = temp[2]
            comunityOfRandnode = gFunction.lookCommunity(detail_infor, randomSelectedNode)

            # Compute the modularity of flipping
            if addOrDelete == 1:
                temp_Q = max(gFunction.computQofDelete(detail_infor, comunityOfRandnode))

            else:
                temp_Q = max(gFunction.computQofAdd(detail_infor, comunityOfRandnode))

            # flipping
            if abs(aim_Q - temp_Q) < abs(aim_Q - Q):
                if addOrDelete == 1:
                    G.delete_edges([(int(gFunction.idToIndex(G, randomSelectedNode[0])), int(gFunction.idToIndex(G, randomSelectedNode[1])))])
                    Q = temp_Q
                else:
                    G.add_edges([(int(gFunction.idToIndex(G, randomSelectedNode[0])), int(gFunction.idToIndex(G, randomSelectedNode[1])))])
                    Q = temp_Q

            # flipping with the decrease probability
            else:
                if random.random() < pow(math.e, (-t/T)):
                    if addOrDelete == 1:
                        G.delete_edges([(int(gFunction.idToIndex(G, randomSelectedNode[0])),int(gFunction.idToIndex(G, randomSelectedNode[1])))])
                        Q = temp_Q
                    else:
                        G.add_edges([(int(gFunction.idToIndex(G, randomSelectedNode[0])),int(gFunction.idToIndex(G, randomSelectedNode[1])))])
                        Q = temp_Q
    return G

