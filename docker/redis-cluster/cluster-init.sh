#!/bin/zsh

#Deleting logs from previous run
rm -rf ./logs/*/redis.log

nodes_startup=$(docker-compose up -d) 2>&1

if [[ $? -ne 0 ]]; then
  echo "Failed to start"
  exit 1
fi

#echo "Waiting for the nodes to completely start before setting up cluster"
sleep 10

docker exec -it redis-node-1 redis-cli \
    --cluster create redis-node-1:6001 redis-node-2:6002 redis-node-3:6003 \
    --cluster-replicas 0


echo "Redis cluster setup done!"