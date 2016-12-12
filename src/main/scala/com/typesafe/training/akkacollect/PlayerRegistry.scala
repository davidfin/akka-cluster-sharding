/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.akkacollect

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging, ActorPath, ActorRef, Address, Props, RootActorPath}



object PlayerRegistry {

  case class RegisterPlayer(name: String, props: Props)

  case class PlayerNameTaken(name: String)

  case class PlayerRegistered(name: String)

  case object GetPlayers

  case class Players(players: Set[String])

  val name: String =
    "player-registry"
  
  def pathFor(address: Address): ActorPath =
    RootActorPath(address) / "user" / name

  def props: Props =
    Props(new PlayerRegistry)
}

class PlayerRegistry extends Actor with ActorLogging {

  import PlayerRegistry._

  private val sharding = PlayerSharding(context.system)

  override def receive: Receive = receive(Players(Set.empty[String]))

  def receive(playerCollection: Players) : Receive = {
    case RegisterPlayer(name, _) if isNameTaken(name, playerCollection) => playerNameTaken(name: String)
    case RegisterPlayer(name, props)                  => registerPlayer(name, props, playerCollection)
    case GetPlayers                                   => sender() ! playerCollection
  }

  private def playerNameTaken(name: String): Unit = {
    log.warning("Player name {} taken", name)
    sender() ! PlayerNameTaken(name)
  }

  private def registerPlayer(name: String, props: Props, playerCollection:Players): Unit = {
    log.info("Registering player {}", name)
    createPlayer(name, props)
    sender() ! PlayerRegistered(name)
    context.become(receive(Players(playerCollection.players + name)))

  }

  protected def createPlayer(name: String, props: Props): Unit =
    // through sharding create it -- player object will create either a random player or simple player
    sharding.tellPlayer(name, PlayerSharding.Player.Initialize(props))

  private def isNameTaken(name: String, playerCollection:Players): Boolean =
    playerCollection.players.contains(name)
}
