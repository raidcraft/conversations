# Conversations Admin Dokumentation

Unterhaltungen im [RCConversations Plugin](../README.md) werden in der [Flow Syntax](https://git.faldoria.de/raidcraft/raidcraft-api/blob/master/docs/ART-API.md#flow-syntax) verfasst.

Eine Unterhaltung ist in verschiedene [Stages](#stages) aufgeteilt. Eine Stage ist als Text des NPCs inkl. der dazugehörigen Antworten definiert. Außerdem kann man beim Starten der Stage und bei der Auswahl einer Antwort [Actions](https://git.faldoria.de/raidcraft/raidcraft-api/blob/master/docs/ART-API.md#actions) ausführen lassen.

- [Basis Config](#basis-config)
- [Stages](#stages)
- [Hosts](#hosts)
- [Flow Syntax](#flow-syntax)
- [Conversation Actions](#conversation-actions)
    - [conversation.start](#conversationstart)
    - [conversation.set](#conversationset)
    - [conversation.clear](#conversationclear)
    - [stage](#stage)
    - [end](#end)
    - [abort](#abort)

## Basis Config

Jede Conversation sollte in einer Datei mit der Endung `.conv.yml` abgespeichert werden. In dieser Datei wird der Ablauf der Unterhaltung definiert, welche dann durch [Hosts](#hosts) oder Actions gestartet werden kann.

> Eine minimale Conversation benötigt nur eine Stage mit dem Namen `start` und sonst keine Konfiguration.

Folgende Konfiguration ist daher vollkommen optional und kann in den meisten Fällen komplett weggelassen werden.

```yml
# Plugins können eigene Conversation Typen schreiben.
# Der Drachenmeister ist z.B. solch eine Conversation.
conv-type: 'default'
# Definiert ob der Spieler die Unterhaltung durch das Wort "exit" beenden darf.
block-end: false
# Blockiert den Start der Unterhaltung. Nützlich um fehlerhafte Unterhaltungen temporär zu sperren.
# Actions können die Unterhaltung weiterhin manuell starten.
# Dieses Flag bezieht sich nur auf das Anklicken eines NPCs der eine Conversation besitzt.
block-start: false
# Wird autmatisch false gesetzt wenn block-end: true ist.
# Beendet die Unterhaltung wenn der Spieler vom NPC wegläuft.
end-out-of-range: true
# Speichert die aktuelle Stage wenn die Unterhaltung abgebrochen wird.
# Wenn der Spieler die Unterhaltung wieder startet fährt sie an der selben Stelle fort.
# Die Unterhaltung muss dann zwingend mit der !end Action beendet werden.
persistant: false
# Umso höher die Priorität um so eher wird die Unterhaltung genommen, falls der NPC mehrere besitzt.
priority: 1
# Beendet die Unterhaltung automatisch sobald keine neue Stage gestartet wird
# oder es keine Antworten mehr gibt.
auto-end: true
# Für Unterhaltungen die direkt ihren Host mit definieren.
# Ist im Quest Plugin irrelevant, da jeder Host seine eigenen Einstellungen hat.
host-settings: ... # siehe Host Dokumentation
# Nur wenn diese Requirements erfüllt sind kann der Spieler die Unterhaltung starten.
requirements: ...
# Diese Actions werden beim Start der Unterhaltung ausgeführt.
actions: ...
# Definiert die Stages in einer Unterhaltung.
stages: ... # Siehe Stages Dokumentation
```

## Stages

Jede Unterhaltung benötigt mindestens eine Stage. Wird nichts anderes definiert versucht eine Unterhaltung Standardmäßig die Stage mit der id `start` zu starten. Gibt es keine Stage mit dem Namen `start` muss die Conversation direkt über eine Action mit der richtigen Stage gestartet werden.

```yml
# Der Text wird beim Starten der Stage zusammen mit dem Host Namen angezeigt.
# Der Text ist optional und kann weggelassen werden.
text: "Hallo mein Freund. Was kann ich für dich tun?"
# Zeigt direkt nach dem Text automatisch die definierten Antworten an.
# Wenn nicht definiert, werden immer alle Antworten direkt angezeigt.
auto-show-answers: true
# Eine Liste von Requirements die erfüllt sein müssen damit die Stage gestartet wird.
# Wird eigentlich kaum benötigt, da Stages immer direkt gestartet werden.
requirements: ...
# Eine Liste von Actions die beim Start der Stage ausgeführt werden.
# Kann auch dazu verwendet werden direkt einen Antwort Block in der Flow Syntax zu verfassen.
actions: ...
# Eine Liste mit zufälligen Actions von denen jedes Mal beim Aufruf der Stage eine ausgeführt wird.
# Kann z.B. dafür genutzt werden um NPCs zufällig Sätze sagen zu lassen.
random-actions: ...
# Eine Liste mit Antworten die in der Stage angezeigt werden.
answers:
    '1':
        # Diesen Antwort Text kann der Spieler anklicken.
        text: 'Nichts, danke!'
        # Requirements die erfüllt sein müssen, damit die Antwort angezeigt wird.
        requirements: ...
        # Die Actions werden beim Auswählen der Antwort ausgeführt.
        actions: ...
    '2':
        text: 'Ich brauche Geld...'
```

## Hosts

In den meisten Fällen sollte eine Conversation an einen Host (NPC) gebunden sein. Wenn sich der Spieler von dem NPC entfernt wird die Unterhaltung beendet. Wenn er den NPC anklickt wird die Unterhaltung gestartet.

> Die Position der NPCs wird direkt in der `.host.yml` Datei festgelegt.

```yml
# NPC Namen dürfen maximal 15 Zeichen lang sein.
name: 'Host Name'
# Kann verwendet werden um den NPC zu verwandeln.
# Muss ein valider Type von https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html sein.
entity-type: PLAYER
# Definiert eine Standard Unterhaltung für den Host.
default-conv: this.host.default
# Wenn auf false kann der NPC angegriffen werden.
protected: true
# Wenn true redet der NPC automatisch mit Spielern in der Nähe.
talk-close: false
# Wenn true verfolgt der NPC den Spieler mit den Augen.
look-close: false
# Die Ausrüstung des NPCs
equipment:
    # Hier können auch Custom Items equiped werden.
    # Der Schaden von Custom Waffen wird verwendet!
    hand: AIR
    # Hier kann auch ein Custom Head verwendet werden, z.B.:
    # skull:Silthus für Köpfe mit Spielernamen
    # oder durch Verwendung der Seite https://minecraft-heads.com/ auch Custom Heads mit dem Base64 String
    # skull:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQxY2I5ZTBhMDRhODRkZGE0ZTcxODhkYzE5MTVlY2JmNmZhYjlhNDAxZTUyNTFjNjYyMDI4N2MxZGZmYTc4NCJ9fX0=
    head: AIR
    chest: AIR
    legs: AIR
    boots: AIR
# Die Position an der der NPC spawnen soll.
location:
    x: 1
    y: 2
    z: 3
    world: world
```

> Die Endung `.default.conv.yml` hat keine spezielle Auswirkung und dient nur zu Übersichtlichkeit.

## Flow Syntax

So wie in der [ART API](https://git.faldoria.de/raidcraft/raidcraft-api/blob/master/docs/ART-API.md) können auch Conversations in der Flow Syntax geschrieben werden. Dafür muss allerdings der `actions` Block in einer Stage und **nicht** der `answers` Block verwendet werden.

```yml
...
stages:
    start:
        # Mann kan den Text auch direkt als Action anzeigen, anstatt in der Stage Config...
        actions:
            - '!text Hallo mein Freund. Was kann ich für dich tun?'
            - ':"Nichts, danke!"'
            - '!end'
            - ':"Ich brauche Geld..."'
            - '!stage geld'
    geld:
        # ...oder normal in der Stage und dann entsprechende Actions ausführen.
        text: 'Hah, geh sterben!'
        actions:
            - '!player.kill'
            - '~2s'
            - '!text "Wer bettelt, der stirbt..."'
```

## Conversation Actions

Es gibt einige spezielle Actions die nur in Conversations verwendet werden können und damit den Ablauf einer Unterhaltung beeinflussen.

### conversation.start

Startet die angegebene Unterhaltung, optional mit einem Host und einer Stage.

```yml
actions:
    # Startet die Unterhaltung mit der Config im selben Ordner und dem Dateinamen
    # conv-template.conv.yml. Default wird die Stage 'start' gestartet.
    - '!conversation.start this.conv-template'
    # Startet die Unterhaltung aus einem Überordner mit dem Namen conv-template.conv.yml
    # Als Host wird der NPC mit der Config im gleichen Ordner
    # und dem Dateinamen host.host.yml verwendet.
    - '!conversation.start ../conv-template this.host'
    # Analog der anderen, mit dem Unterschied dass direkt die Stage 'stage1' gestartet wird.
    - '!conversation.start conv:this.conv-template host:this.host stage:stage1'
```

### conversation.set

Setzt für den Spieler und Host eine Conversation die bei der nächsten Interaktion gestartet wird. Das ist z.B. sehr nützlich um nach oder während den [Quests](https://git.faldoria.de/plugin-configs/quests/blob/develop/docs/QUEST-DEVELOPER.md) den NPC andere Dinge sagen zu lassen.

```yml
actions:
    # Setzt für den Spieler die angegbene Unterhaltung am angegebenen Host.
    '!conversation.set conv:this.conv-template host:this.host'
```

### conversation.clear

Entfernt manuelle gesetzt Unterhaltungen auf einem Host. Nützlich wenn man mit `conversation.set` eine Unterhaltung gesetzt hat und diese wieder entfernen will.

```yml
actions:
    # Entfernt alle manuell gesetzten Unterhaltungen am NPC host.
    '!conversation.clear this.host'
```

### stage

Mit der `stage` Action lässt es sich innerhalb einer Conversation die Stage wechseln.

```yml
actions:
    # Wechselt in die Foobar Stage
    - '!stage foobar'
```

### end

Die Action `!end` beendet die aktuelle Conversation.

### abort

Die Action `!abort` bricht die aktuelle Conversation ab.