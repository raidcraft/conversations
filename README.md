# Conversations Plugin

Das Conversations Plugin ermöglicht eine Unterhaltung zwischen Spielern und [Conversation Hosts](docs/ADMIN.md#hosts). Ein Host kann z.B. ein NPC oder ein Mob sein. Die Unterhaltung wird dafür in der [Flow Syntax](https://git.faldoria.de/raidcraft/raidcraft-api/blob/master/docs/ART-API.md#flow-syntax) gescriptet und ermöglicht es [Requirements](https://git.faldoria.de/raidcraft/raidcraft-api/blob/master/docs/ART-API.md#requirements) zu prüfen und [Actions](https://git.faldoria.de/raidcraft/raidcraft-api/blob/master/docs/ART-API.md#actions) auszuführen.

- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)

## Getting Started

* [Project Details](https://git.faldoria.de/raidcraft/rcconversations)
* [Source Code](https://git.faldoria.de/raidcraft/rcconversations/tree/master)
* [Latest Stable Download](https://ci.faldoria.de/view/RaidCraft/job/RCConversations/lastStableBuild)
* [Issue Tracker](https://git.faldoria.de/raidcraft/rcconversations/issues)
* [Developer Documentation](docs/DEVELOPER.md)
* [Admin Documentation](docs/ADMIN.md)

### Prerequisites

Das `RCConversations` Plugin ist von der [RaidCraft API](https://git.faldoria.de/raidcraft/raidcraft-api) abhängig und benötigt eine Verbindung zu einer MySQL Datenbank um die Spawn Punkte der [Conversation Hosts](docs/ADMIN.md#hosts) zu speichern.

### Installation

Beim ersten Start des Servers wird eine `database.yml` und eine `config.yml` angelegt. Am besten den Server direkt nochmal stoppen und die Angaben in der `database.yml` bearbeiten.

Die `config.yml` enthält folgende defaults:

```yml
# Ein Standard Text der ausgegeben wird wenn der Spieler eine falsche Antwort eingibt.
wrong-answer-warning: Ich habe deine Antwort nicht verstanden!
# Ein Wort das der Spieler während der Conversation eingeben kann um die Unterhaltung zu verlassen.
conversation-exit-word: exit
# In dieser Entfernung zum Host wird der Spieler gewarnt, dass die Unterhaltung gleich abgebrochen wird.
conversation-warn-distance: 6
# Ab dieser Distanz zum Host wird die Conversation abgebrochen.
conversation-end-distance: 10
# Die Distanz in der NPCs einen Spieler ansprechen, wenn das talk-nearby Flag gesetzt ist.
npc-talk-nearby-distance: 5
# Der Cooldown in Sekunden bis ein NPC einen Spieler wieder anspricht.
npc-talk-nearby-cooldown: 30
# Cooldown in Sekunden bis ein Spieler wieder eine Conversation starten kann.
# Verhindert Server Lags
conversation-cooldown: 1.0
```