
## Cluster Sharding 

Horizontally partioning actors into many smaller sets possibly in different physical locations. This allows the actors to balance resources across multiple shards for scalability. 

### Entity 
individual actor assigned a unique id
### Shard 
A group of entities managed together 
### ShardRegion

An actor started on each node (that is participating in Cluster Sharding) and is the parent for a series of shards. Shard regions route messages to its shards which in turn routes messages to their entities. 

## Shard Coordinator
This cluster singleton designates which ShardRegion will host specific Shards. NOTE: potential resource contention and additional latency may occur. The coordinator makes use of Akka Persistence by storing information about ShardRegion ownership over Shards. Persistence can also be used by the entities to prevent data loss during system restarts/migrations. 


## Sending messages to sharded actors: 

Must provide the unique id and the message (payload). Shard and id extractors must also be implemented: 

```scala
val myActorShardRegion = ClusterSharding(system).shardRegion("my-actor")
myActorShardRegion ! Envelope("id", "payload")

val entityIdExtractor: ShardRegion.ExtractEntityId = {
  case Envelope(id, payload) => (id, payload)
}

def shardIdExtractor(shardCount: Int): ShardRegion.ExtractShardId = {
  case Envelope(id, _) => (id.hashCode % shardCount).toString
}
```

## Cluster Rebalancing 

All ShardRegions are notified and all entities on rebalanced shards are stopped and restarted on new ShardRegions. 

## Pasivation 
```scala
ShardRegion.Passivate 
```
Can be used to gracefully restart actors, the ShardRegion buffers incoming messages during actor shut down and then delivers them upon actor restart. 

## Exercise
In this exercise we use a Cluster Sharding to split off the Player actors.

1. Player actors are not referred to using ActorRef anymore, the tellPlayer method from PlayerSharding.scala must be used. 
2. Create a Journal Actor and start the PlayerSharding proxy in GameEngineApp.scala 
3. update PlayerRegistry.scala to use the tellPlayer method in PlayerSharding.scala to communicate with Players. Also maintain a set of local Players. 
4. start PlayerSharding in PlayerRegistryApp.scala
5. Add shard count to Settings.scala




