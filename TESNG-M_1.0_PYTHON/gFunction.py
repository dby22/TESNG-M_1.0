
# author: byduan
# function: The method of generator
import random


def getdetail(G):
    '''
    Get the detail information of the G
    :param filename:
    :return:
    '''

    commitname = list(set(G.vs()['value']))
    commit_count = len(commitname)
    edge_list_index = list()
    edge_list = list()
    eachCommunityNodeList = [[] for _ in range(commit_count)]
    in_edge = [[] for _ in range(commit_count)]
    between_edge = [[] for _ in range(commit_count)]
    detail_info = [[] for _ in range(commit_count)]

    node_list = list()
    for i in range(len(commitname)):
        eachCommunityNodeList[i] = G.vs.select(value=commitname[i])['id']
        for j in range(len(eachCommunityNodeList[i])):
            node_list.append(int(eachCommunityNodeList[i][j]))

    for e in G.es:
        edge_list_index.append(e.tuple)

    for i in range(len(edge_list_index)):
        edgeA = int(indexToId(G,edge_list_index[i][0]))
        edgeB = int(indexToId(G,edge_list_index[i][1]))
        edge_list.append((edgeA,edgeB))


    for i in edge_list:
        for commit in eachCommunityNodeList:
            if int(i[0]) in commit and int(i[1]) in commit:
                in_edge[eachCommunityNodeList.index(commit)].append((int(i[0]),int(i[1])))
                break
            if int(i[0]) in commit and int(i[1]) not in commit:
                between_edge[eachCommunityNodeList.index(commit)].append((int(i[0]),int(i[1])))
            if int(i[0]) not in commit and int(i[1]) in commit:
                between_edge[eachCommunityNodeList.index(commit)].append((int(i[0]),int(i[1])))

    for i in range(commit_count):
        detail_info[i].append(len(eachCommunityNodeList[i]))
        detail_info[i].append(len(in_edge[i]))
        detail_info[i].append(len(between_edge[i]))
        detail_info[i].append(eachCommunityNodeList[i])
        detail_info[i].append(in_edge[i])
        detail_info[i].append(between_edge[i])
    return node_list, edge_list, detail_info

def computQ(G):
    '''
    compute modularity
    :param detail_info:
    :return: Q
    '''
    q = list()
    detail_info = getdetail(G)

    m = len(detail_info[1])
    for i in range(len(detail_info[2])):
        q.append(detail_info[2][i][1]/m - ((2*detail_info[2][i][1] + detail_info[2][i][2])/(2*m))**2)
    Q = sum(q)
    return  Q

def judge(G):
    '''
    compute whether there is an edge between the selected nodes
    :param start_detail_info:
    :return:
    '''
    start_detail_info = getdetail(G)
    node_list = start_detail_info[0]
    edge_list = start_detail_info[1]
    temp1 = random.sample(node_list, 2)
    nodeA = temp1[0]
    nodeB = temp1[1]
    if (nodeA,nodeB) in edge_list or (nodeB,nodeA) in edge_list:
        return 1, temp1, start_detail_info
    else:
        return 0, temp1, start_detail_info

def lookCommunity(start_detail_info,random_node):
    '''
    compute the selected nddes belong to witch community
    :param start_detail_info:
    :param random_node:
    :return:
    '''
    communityOfnodeA, communityOfnodeB = 0,0
    for i in range(len(start_detail_info[2])):
        if random_node[0] in start_detail_info[2][i][3]:
            communityOfnodeA = i
        if random_node[1] in start_detail_info[2][i][3]:
            communityOfnodeB = i
    return communityOfnodeA, communityOfnodeB

def computQofDelete(detail_info,random_community):
    '''
    compute the change of modularity because the edges deletion
    :param detail_info:
    :param random_community:
    :return:
    '''

    m = len(detail_info[1])
    q2 = 0
    qqq = 0
    # compute the change of modularity because the intra-community edge deletion
    if random_community[0] == random_community[1]:
        for i in range(len(detail_info[2])):
            if i == random_community[0]:
                q2 = q2 + ((detail_info[2][i][1]-1)/(m-1)-((2*detail_info[2][i][1]-2+detail_info[2][i][2])/(2*m-2))**2)
            else:
                q2 = q2 + ((detail_info[2][i][1])/(m-1)-((2*detail_info[2][i][1]+detail_info[2][i][2])/(2*m-2))**2)
    # compute the change of modularity because the inter-community edge deletion
    else:
        for i in range(len(detail_info[2])):
            if i == random_community[0] or i == random_community[1]:
                qqq = qqq + ((detail_info[2][i][1]/(m-1)-((2*detail_info[2][i][1]+detail_info[2][i][2]-1)/(2*m-2))**2))
            else:
                qqq = qqq + ((detail_info[2][i][1])/(m-1)-((2*detail_info[2][i][1]+detail_info[2][i][2])/(2*m-2))**2)

    return q2,qqq

def computQofAdd(detail_info,random_community):
    '''
    compute the change of modularity because the change insertion
    :param detail_info:
    :param random_community:
    :return:
    '''
    m = len(detail_info[1])
    q1 = 0
    qq = 0
    # compute the change of modularity because the intra-community edge insertion
    if random_community[0] == random_community[1]:
        for i in range(len(detail_info[2])):
            if i == random_community[0]:
                q1 = q1+(detail_info[2][i][1]+1)/(m+1)-((2*detail_info[2][i][1]+2+detail_info[2][i][2])/(2*m+2))**2
            else:
                q1 = q1+(detail_info[2][i][1])/(m+1)-((2*detail_info[2][i][1]+detail_info[2][i][2])/(2*m+2))**2
    # compute the change of modularity because the inter-community edge insertion
    else:
        for i in range(len(detail_info[2])):
            if i == random_community[0] or i == random_community[1]:
                qq = qq+(detail_info[2][i][1])/(m+1)-((2*detail_info[2][i][1]+detail_info[2][i][2]+1)/(2*m+2))**2
            else:
                qq = qq+(detail_info[2][i][1])/(m+1)-((2*detail_info[2][i][1]+detail_info[2][i][2])/(2*m+2))**2
    return q1, qq

def idToIndex(G,id):
    '''
    map id to index
    :param G:
    :param id:
    :return:
    :function
    '''

    index = 0
    nodeIdLidt = list()
    commitname = list(set(G.vs()['value']))
    for commit in commitname:
        for i in range(len(G.vs.select(value=commit)['id'])):
            nodeIdLidt.append(G.vs.select(value=commit)['id'][i])

    nodeIdLidt = sorted(nodeIdLidt)
    if id in nodeIdLidt:
        for i in range(len(nodeIdLidt)):
            if nodeIdLidt[i]<id:
                index = index + 1
        return index
    else:
        return -1
    pass

def indexToId(G,index):
    '''
    map index to id
    :param G:
    :param index:
    :return:
    :function:
    '''
    nodeIdLidt = list()
    commitname = list(set(G.vs()['value']))
    for commit in commitname:
        for i in range(len(G.vs.select(value=commit)['id'])):
            nodeIdLidt.append(G.vs.select(value=commit)['id'][i])
    nodeIdLidt = sorted(nodeIdLidt)
    id = nodeIdLidt[index]
    return id
    pass

def writeQtoTXT(Q_LIST):
    '''
    Write to gml
    :param Q_LIST:
    :return:
    :function:
    '''
    line = ''
    for i in Q_LIST:
        line = line + str(i) + ' '
    with open('./result/Q_list.gml','w') as f:
        f.writelines(line)
    pass












