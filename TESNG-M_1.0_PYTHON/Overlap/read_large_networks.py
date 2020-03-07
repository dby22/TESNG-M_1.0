import igraph

def yan_read_large_network():
    '''
    Read file
    :return:
    '''
    filename = '../data/large_network/com-amazon.ungraph.txt'
    filenamecmty = '../data/large_network/com-amazon.top5000.cmty.txt'
    V = set()
    E = set()
    with open(filename,'r') as f:
        for ignose in range(4):
            f.readline()
        for date in f.readlines():
            nodetrip = date.strip('\r\n').split('\t')
            nodeA = nodetrip[0]
            nodeB = nodetrip[1]
            V.add(nodeA)
            V.add(nodeB)
            if (nodeA,nodeB) not in E and (nodeB,nodeA) not in E:
                E.add((nodeA,nodeB))
    # Read the information of top5000 community and establish the dict of nodes and it's community
    valuedict = dict()
    with open(filenamecmty,'r') as f:
        readfile = f.readlines()
        for line in range(5000):
            cmty = readfile[line].strip('\r\n').split('\t')
            for node in cmty:
                if node not in valuedict.keys():
                    valuedict[node] = str(line)
                else:
                    newvalue = valuedict[node] + ',' + str(line)
                    valuedict[node] = newvalue
    # Add the nodes into the G
    V = list(V)
    E = list(E)
    newE = list()
    G = igraph.Graph(directed=False)
    for i in range(len(V)):
        if V[i] in valuedict.keys():
            G.add_vertex(name=V[i], value=None)
    for i in range(len(E)):
        if E[i][0] in valuedict.keys() and E[i][1] in valuedict.keys():
            newE.append(E[i])
    G.add_edges(newE)

    # Set the attribute of community for each nodes
    for i in valuedict.keys():
        G.vs().find(name=i).update_attributes({'value': valuedict[i]})
    G.write_gml('../data/dblp.gml')
    pass

if __name__ == '__main__':
    yan_read_large_network()