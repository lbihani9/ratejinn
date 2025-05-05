#!/bin/zsh

#Deleting logs from previous run
rm -rf ./logs/*/redis.log

nodes_startup=$(docker-compose up -d) 2>&1

if [[ $? -ne 0 ]]; then
  echo "Failed to startup redis nodes"
  exit 1
fi

echo "Waiting for the nodes to completely start before setting up the cluster..."
sleep 5

# Setting up cluster
cluster_setup=$(docker exec -it redis-node-1 redis-cli \
    --cluster create redis-node-1:6001 redis-node-2:6002 redis-node-3:6003 \
    --cluster-replicas 0 --cluster-yes) 2>&1

if [[ $? -ne 0 ]]; then
  echo "Failed to setup redis cluster. Tearing down redis docker nodes..."
  docker-compose down -v
  exit 1
fi

echo "Redis cluster is ready!"
echo "Here are the cluster details: "
docker exec -it redis-node-1 redis-cli -p 6001 cluster nodes

