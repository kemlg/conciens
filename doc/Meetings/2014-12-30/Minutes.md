

## Event list with their TTLS

### EVENT_TYPE_EMOTE,						
//  0
Set of predetermined expressions (dance, point at).
TTL:  10 minutes
### EVENT_TYPE_ITEM_USE,						
//  1
Self explanatory. Only applies to inventory objects
TTL:  48 hours
### EVENT_TYPE_ITEM_EXPIRE,						
//  2
An item has disappeared from the world (consumed, time out, etc.) Contains item template rather than item-id.
TTL:  48 hours
### EVENT_TYPE_GOSSIP_HELLO,					
//  3
Open NPC interaction window
TTL:  10 minutes
### EVENT_TYPE_GOSSIP_SELECT,					
//  4
Select one option on the NPC interaction window
TTL:  10 minutes
### EVENT_TYPE_GOSSIP_SELECT_CODE,					
//  5
Insert special promotional code on the NPC interaction window (Irrelevant)
TTL: 0, never in cache
### EVENT_TYPE_GOSSIP_HELLO_OBJECT,					
//  6
Just like EVENT_TYPE_GOSSIP_HELLO,
TTL:  10 minutes
### EVENT_TYPE_GOSSIP_SELECT_OBJECT,				
//  7
Just like EVENT_TYPE_GOSSIP_SELECT,
TTL:  10 minutes
### EVENT_TYPE_GOSSIP_SELECT_CODE_OBJECT,				
//  8
Just like EVENT_TYPE_GOSSIP_SELECT_CODE,
TTL:  10 minutes
### EVENT_TYPE_QUEST_ACCEPT,					
//  9
A player has accepted a particular quest.
TTL:  48 hours
### EVENT_TYPE_QUEST_SELECT,					
// 10
A player has initiated a quest on the menu (previous to accept)
TTL:  10 minutes
### EVENT_TYPE_QUEST_STATUS_CHANGE,					
// 11
One the quest is active, reflects changes in its status (None, Complete, Incomplete, Failed, Reward)
TTL:  1 minute
### EVENT_TYPE_QUEST_REWARD,					
// 12
Player is receiving reward for the quest.
TTL:  10 minutes
### EVENT_TYPE_GET_DIALOG_STATUS,					
// 13
Client asks server which is the status between player and NPC/object.
TTL:  10 minutes
### EVENT_TYPE_QUEST_ACCEPT_OBJECT,					
// 14
Equivalent to 9
TTL:  48 hours
### EVENT_TYPE_QUEST_SELECT_OBJECT,					
// 15
Equivalent to 10
TTL:  10 minutes
### EVENT_TYPE_QUEST_COMPLETE_OBJECT,				
// 16
Legacy event (not relevant)
TTL: 0, never in cache
### EVENT_TYPE_QUEST_REWARD_OBJECT,					
// 17
Equivalent to 12
TTL:  10 minutes
### EVENT_TYPE_GET_DIALOG_STATUS_OBJECT,				
// 18
Equivalent to 13
TTL:  10 minutes
### EVENT_TYPE_OBJECT_CHANGED,					
// 19
Asynchronous change in object (possible states are ‘active’ used and not usable again (door is opened and can not be closed), ‘ready’ ready to be used (door is closed and can be opened), ‘active by alternative means’ (same as active but via non-traditional or expected ways, door has been open by shooting at it)). Focused on object visualization.
TTL:  0.1 minutes
### EVENT_TYPE_OBJECT_UPDATE,					
// 20
Loop checking objects on the same zone as the player. When object is updated via //19, a map with timestamp difference between updates is provided to the object, and the function updates the objects.
TTL:  0.1 minutes
### EVENT_TYPE_AREA_TRIGGER,					
// 21
Kind of landmine that activates a particular event when player steps on an area
TTL:  0.2 minutes
### EVENT_TYPE_WEATHER_CHANGE,					
// 22
Event introduced into the system everytime there is a weather change. Check weather.cpp between 200 and 260.
TTL:  5 minutes
### EVENT_TYPE_WEATHER_UPDATE,					
// 23
Equivalent to 20 on weather change.
TTL:  5 minutes
### EVENT_TYPE_PVP_KILL,						
// 24
A player has killed another player
TTL:  1 week
### EVENT_TYPE_CREATURE_KILL,					
// 25
Like 24 for living being (not only PVP)
TTL:  1 hour
### EVENT_TYPE_KILLED_BY_CREATURE,					
// 26
Player has been killed by a living being
TTL:  24 hour
### EVENT_TYPE_MONEY_CHANGED,					
// 27
Money in possession of a player has changed
TTL:  10 minutes
### EVENT_TYPE_LEVEL_CHANGED,					
// 28
Level of a player has changed
TTL:  4 hours
### EVENT_TYPE_CREATURE_UPDATE,	
// 29
Like 20, but not a subtype of it.
TTL:  0.1 minutes
### EVENT_TYPE_PLAYER_UPDATE,					
// 30
Like 29, but not a subtype of it
TTL:  0.1 minutes
### EVENT_TYPE_ITEM_REMOVE,						
// 31
Player has destroyed an item that belongs to him
TTL:  12 hours
### EVENT_TYPE_GAME_OBJECT_DESTROYED,				
// 32
Player has destroyed an item that does not belong to him (mine, door, etc.)
TTL:  24 hours
### EVENT_TYPE_GAME_OBJECT_DAMAGED,
// 33
Like 32 but damaging an object instead of destroying it
TTL:  0.5 minutes
### EVENT_TYPE_GAME_OBJECT_LOOT_STATE_CHANGED,			
// 34
Similar to 19 focused on object interaction. Possible states are: READY, ACTIVATED, JUST_DEACTIVATED)
TTL:  0.5 minutes
### EVENT_TYPE_AUCTION_ADD,						
// 35
On a particular auction house, someone added an item to the auction. Auction starts
TTL: 48 Hours
### EVENT_TYPE_AUCTION_REMOVE,
// 36
Complement to 35, auction stops and is cancelled.
TTL: 48 Hours
### EVENT_TYPE_AUCTION_SUCCESSFUL,
// 37
Auction is over, object is sold
TTL: 48 Hours
### EVENT_TYPE_AUCTION_EXPIRE,
// 38
Complement to 37, auction is over, item is not sold
TTL: 48 Hours
### EVENT_TYPE_PLAYER_CHAT,
// 39
A player has announced something on the chat system
TTL: 10 minutes
### EVENT_TYPE_PLAYER_SPELL_CAST,
// 40
A player has enacted a spell successfully
TTL:  5 minutes
### EVENT_TYPE_PLAYER_LOGIN,					
// 41
A player enters the realm
TTL:  1 hour
### EVENT_TYPE_PLAYER_LOGOUT,
// 42
Complement to 42, a player leaves the realm
TTL:  1 hour
### EVENT_TYPE_PLAYER_CREATE,
// 43
A player creates an avatar (has nothing to do with the account, but the player themselves)
TTL:  48 hours
### EVENT_TYPE_PLAYER_DELETE,
// 44
Complement to 43
TTL:  48 hours
### EVENT_TYPE_PLAYER_SAVE,
// 45
Player state dumped to persistent DB
TTL:  1 minute
### EVENT_TYPE_PLAYER_UPDATE_ZONE,
// 46
Player has leaved a particular zone and entered a new one
TTL:  5 minutes
### EVENT_TYPE_HEAL,
// 47
Some living being has healed a living being (includes himself)
TTL:  0.5 minutes
### EVENT_TYPE_DAMAGE,
// 48
Complementary to 47. Some living being has damaged a living being.
TTL:  0.5 minutes	

