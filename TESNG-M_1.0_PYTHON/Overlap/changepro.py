import threadpool
import math
import random
import igraph
import time
def computL(i,j,cmty,c):
    '''
    compute r_i,j,c
    :param flag:
    :param cmty_i:
    :param cmty_j:
    :return:
    '''

    if c in cmty[i] and c in cmty[j]:
        if ',' in cmty[i]:
            cmty_i_list = cmty[i].split(',')
        else:
            cmty_i_list = [cmty[i]]
        p_ic = 1 / len(cmty_i_list)
        if ',' in cmty[j]:
            cmty_j_list = cmty[j].split(',')
        else:
            cmty_j_list = [cmty[j]]
        p_jc = 1 / len(cmty_j_list)

    elif c not in cmty[i] and c in cmty[j]:
        p_ic = 0
        if ',' in cmty[j]:
            cmty_j_list = cmty[j].split(',')
        else:
            cmty_j_list = [cmty[j]]
        p_jc = 1 / len(cmty_j_list)


    elif c in cmty[i] and c not in cmty[j]:
        if ',' in cmty[i]:
            cmty_i_list = cmty[i].split(',')
        else:
            cmty_i_list = [cmty[i]]
        p_ic = 1 / len(cmty_i_list)
        p_jc = 0

    else:
        p_ic = 0
        p_jc = 0

    return 1 / ((1 + pow(math.e, -(60 * p_ic - 30))) * (1 + pow(math.e, -(60 * p_jc - 30))))

def computW(I,J,nodelist,cmty,c):
    '''
    compute w_i,j
    :param I:
    :param J:
    :param nodelist:
    :param cmty:
    :param c:
    :return:
    '''
    firstmol = 0
    secondmol = 0
    for i in range(len(nodelist)):
        firstmol = firstmol + computL(i,J,cmty,c)
    for j in range(len(nodelist)):
        secondmol = secondmol + computL(I,j,cmty,c)

    return (firstmol/(len(nodelist)))*(secondmol/len(nodelist))

def index_idofname(G):
    mydict = dict()
    for i in G.vs()['id']:
        mydict[i] = G.vs().find(id=i)['name']
        print(i,mydict[i])
    return mydict
    pass

def index_nameofid(G):
    myrevlict = dict()
    for i in G.vs()['name']:
        myrevlict[i] = G.vs().find(name=i)['id']
    return myrevlict

def getdetail(G):
    '''
    Detail information of the community
    :param G:
    :return:
    '''
    mydict = index_idofname(G)  # {id:name}
    revmydict = index_nameofid(G)  # {name:id}
    nodelist = G.vs()['name']
    edgelistid = G.get_edgelist()
    edgelist = list()
    for edge in edgelistid:
        edgelist.append((int(mydict[edge[0]]), int(mydict[edge[1]])))
    cmty = list()
    for i in range(len(nodelist)):
        cmty.append(G.vs().find(name=nodelist[i])['value'])
    m = len(edgelist)
    neigbor_size = [G.neighborhood_size(int(revmydict[_])) for _ in nodelist]
    return nodelist,edgelist,cmty,mydict,revmydict,neigbor_size,m

def computQ(G):
    '''
    compute Q
    :param G:
    :return:
    '''
    temp = getdetail(G)
    nodelist = temp[0]
    edgelist = temp[1]
    cmty = temp[2]
    revmydict = temp[4]
    neigbor_size = temp[5]
    m = temp[6]
    Q = [[[] for k in range(len(nodelist))] for j in range(5000)]

    for c in range(5000):
        for i in range(len(nodelist)):
            for j in range(len(nodelist)):
                if (nodelist[i], nodelist[j]) in edgelist or (nodelist[j], nodelist[i]) in edgelist:
                    A_ij = 1
                else:
                    A_ij = 0
                q1 = computL(i, j, cmty, str(c)) * A_ij
                q2 = (computW(i, j, nodelist, cmty, str(c)) *
                      neigbor_size[int(revmydict[nodelist[i]])] * neigbor_size[int(revmydict[nodelist[j]])])

                Q[c][i].append([q1, q2])
    sum1 = 0
    sum2 = 0
    for c in range(5000):
        for i in range(len(nodelist)):
            sum1 = sum1 + sum([Q[c][i][j][0] for j in range(len(nodelist))])
            sum2 = sum2 + sum([Q[c][i][j][1] for j in range(len(nodelist))])
    final_Q = sum1/(2*m) - sum2/(4*m)
    return final_Q,Q




def changenode(G,p_exchange):
    '''
    NodeEvolution
    :param G:
    :param p_exchange:
    :return:
    '''

    nodelist = G.vs()['name']
    # Insert nodes
    if random.random() < p_exchange:
        maxcount = math.ceil(len(nodelist) * 0.02)
        insertNodeNumber = random.randint(0, maxcount)
        if insertNodeNumber != 0:
            # Connected the inserted nodes with others of inserted community
            for i in range(insertNodeNumber):
                insertedge = list()
                insertname = int(max(nodelist) + i + 1)
                select_community = random.sample(range(5000), 1)[0]
                with open('../data/large_network/com-dblp.top5000.cmty.txt', 'r') as f:
                    readfile = f.readlines()
                    cmty = readfile[select_community].strip('\r\n').split('\t')
                G.add_vertex(name=str(insertname),value=select_community)
                for _ in range(len(cmty)):
                    insertedge.append((insertname,cmty[_]))
                G.add_edges(insertedge)

    # Delete nodes
    else:
        mydict = index_nameofid(G)
        print(mydict)
        maxx = math.ceil(len(nodelist) * 0.02)
        deleteNodeNumber = random.randint(0, maxx)
        if deleteNodeNumber != 0:
            selectDeletNode = random.sample(nodelist, deleteNodeNumber)
            idofselectdeletenode = list()
            for node in selectDeletNode:
                idofselectdeletenode.append(int(mydict[node]))
            G.delete_vertices(idofselectdeletenode)
    pass

def changedge(G,Q,aim_Q,T,detail_Q):
    '''
    EdgeEvolution
    :param G:
    :param Q:
    :param aim_Q:
    :param T:
    :return:
    '''

    mydict = index_idofname(G)
    revmydict = index_nameofid(G)
    nodelist = G.vs()['name']
    edgelistid = G.get_edgelist()
    edgelist = list()
    for edge in edgelistid:
        edgelist.append((mydict[edge[0]],mydict[edge[1]]))  # 构造由name表示的边的列表
    for t in range(100000):
        if abs(aim_Q - Q) < 0.001:
            break
        else:
            nodetrip = random.sample(nodelist,2)
            nodeA = nodetrip[0]
            nodeB = nodetrip[1]
            if (nodeA,nodeB) in edgelist or (nodeB,nodeA) in edgelist:
                subflag = 1
            else:
                subflag = 0
            temp_Q,detail_Q = computtemp_Q(G,subflag,nodeA,nodeB,revmydict,detail_Q)

            # Flipping
            if abs(aim_Q - temp_Q) < abs(aim_Q - Q):
                if subflag == 1:
                    G.delete_edges((revmydict[nodeA],revmydict[nodeB]))
                    Q = temp_Q
                else:
                    G.add_edges((revmydict[nodeA],revmydict[nodeB]))
                    Q = temp_Q

            # Flipping with the decrease probability
            else:
                if random.random() < pow(math.e, (-t / T)):
                    if subflag == 1:
                        G.delete_edges((revmydict[nodeA], revmydict[nodeB]))
                        Q = temp_Q
                    else:
                        G.add_edges((revmydict[nodeA], revmydict[nodeB]))
                        Q = temp_Q
    return G,Q,detail_Q

def computtemp_Q(G,subflag,a,b,revmydict,detail_Q):
    '''
    Compute temp_Q
    :param start:
    :return:
    '''
    temp = getdetail(G)
    nodelist = temp[0]
    cmty = temp[2]
    neigbor_size = temp[5]
    m = temp[6]
 
    if subflag == 1:
        m = m - 1
    elif subflag == 0:
        m = m + 1

    for c in range(5000):
        for i in range(len(nodelist)):
            for j in range(len(nodelist)):
                if subflag == 1:
                    A_ij = 1
                else:
                    A_ij = 0
                if (i, j) == (a, b) or (i, j) == (b, a):
                    q1 = computL(i, j, cmty, str(c)) * A_ij
                    q2 = (computW(i, j, nodelist, cmty, str(c)) *
                          neigbor_size[int(revmydict[nodelist[i]])] * neigbor_size[int(revmydict[nodelist[j]])])
                    detail_Q[c][i][j] = [q1, q2]
                    detail_Q[c][j][i] = [q1, q2]
                    break
            break

    sum1 = 0
    sum2 = 0
    for c in range(5000):
        for i in range(len(nodelist)):
            sum1 = sum1 + sum([detail_Q[c][i][j][0] for j in range(len(nodelist))])
            sum2 = sum2 + sum([detail_Q[c][i][j][1] for j in range(len(nodelist))])
    final_Q = sum1 / (2 * m) - sum2 / (4 * m)

    return final_Q, detail_Q








