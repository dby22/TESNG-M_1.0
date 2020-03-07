import random
import gFunction
import math


def change(G,p_exchange):

    # Read the list of nodes id
    nodeIdList = gFunction.getdetail(G)[0]

    # Insert nodes
    if random.random() < p_exchange:
        maxcount = math.ceil(len(nodeIdList) * 0.02)
        insertNodeNumber = random.randint(0,maxcount)
        if insertNodeNumber != 0:
            # Connected the inserted nodes with the others of inserted community
            for i in range(insertNodeNumber):
                insertEdge_index = list()
                insertId = int(max(nodeIdList) + i + 1)
                select_community = random.sample(range(12), 1)[0]
                insertCommunityNode = G.vs.select(value=select_community).indices

                G.add_vertex(id=int(insertId), value=select_community)
                for i in range(len(insertCommunityNode)):
                    insertEdge_index.append((int(insertCommunityNode[i]), int(gFunction.idToIndex(G, insertId))))  # 插入边的索引列表
                G.add_edges(insertEdge_index)

    # Delete nodes
    else:
        maxx = math.ceil(len(nodeIdList)*0.02)
        deleteNodeNumber = random.randint(0,maxx)

        if deleteNodeNumber != 0:
            selectDeletNode = random.sample(nodeIdList, deleteNodeNumber)
            for i in range(deleteNodeNumber):
                eachDeletNode = gFunction.idToIndex(G, selectDeletNode[i])
                G.delete_vertices([eachDeletNode])
    return G