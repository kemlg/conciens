/*
 * Moon++ Scripts for Ascent MMORPG Server
 * Copyright (C) 2005-2007 Ascent Team <http://www.ascentemu.com/>
 * Copyright (C) 2007-2008 Moon++ Team <http://www.moonplusplus.info/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <stdio.h>

#include "Setup.h"
#include "EventBridge.h"

EventBridge*	eb;
//

void GuardsOnSalute(Player* pPlayer, Unit* pUnit)
{
	if ( pPlayer == NULL || pUnit == NULL )
		return;

	// Check if we are friendly with our Guards (they will salute only when You are)
	if(((pUnit->GetEntry() == 68 || pUnit->GetEntry() == 1976) && pPlayer->GetStandingRank(72) >= STANDING_FRIENDLY) || ( pUnit->GetEntry() == 3296 && pPlayer->GetStandingRank(76) >= STANDING_FRIENDLY))
	{
		uint32 EmoteChance = RandomUInt(100);
		if(EmoteChance < 100) // 1/3 chance to get Salute from Guard
			pUnit->Emote(EMOTE_ONESHOT_RUDE);
			//pUnit->Emote(EMOTE_ONESHOT_SALUTE);
	}
}

void GaurdsOnKiss(Player* pPlayer, Unit* pUnit)
{
	if ( pPlayer == NULL || pUnit == NULL )
		return;

	// Check if we are friendly with our Guards (they will bow only when You are)
	if (((pUnit->GetEntry() == 68 || pUnit->GetEntry() == 1976) && pPlayer->GetStandingRank(72) >= STANDING_FRIENDLY) || (pUnit->GetEntry() == 3296 && pPlayer->GetStandingRank(76) >= STANDING_FRIENDLY))
	{
		uint32 EmoteChance = RandomUInt(100);
		if(EmoteChance < 33) // 1/3 chance to get Bow from Guard
			pUnit->Emote(EMOTE_ONESHOT_BOW);
	}
}

void GuardsOnWave(Player* pPlayer, Unit* pUnit)
{
	if ( pPlayer == NULL || pUnit == NULL )
		return;

	// Check if we are friendly with our Guards (they will wave only when You are)
	if (((pUnit->GetEntry() == 68 || pUnit->GetEntry() == 1976) && pPlayer->GetStandingRank(72) >= STANDING_FRIENDLY) || (pUnit->GetEntry() == 3296 && pPlayer->GetStandingRank(76) >= STANDING_FRIENDLY))
	{
		uint32 EmoteChance = RandomUInt(100);
		if(EmoteChance < 33) // 1/3 chance to get Bow from Guard
			pUnit->Emote(EMOTE_ONESHOT_WAVE);
	}
}

void OnGossipHello(Player* pPlayer, Unit* pUnit)
{
	char	msg[1024];
	uint64	player, unit;

	player = pPlayer->GetGUID();
	if(!pUnit)
	{
		unit = 0;
	}
	else
	{
		unit = pUnit->GetGUID();
	}

	sprintf(msg, "HELLO|%lu|%llu\n", player, unit);
	eb->sendMessage(msg);
}

void OnEmote(Player* pPlayer, uint32 Emote, Unit* pUnit)
{
	char	msg[1024];
	uint64	player, unit;

	player = pPlayer->GetGUID();
	if(!pUnit || !pUnit->isAlive() || pUnit->GetAIInterface()->getNextTarget())
	{
		unit = 0;
	}
	else
	{
		unit = pUnit->GetGUID();
	}

	sprintf(msg, "EMOTE|%u|%lu|%llu\n", Emote, player, unit);
	eb->sendMessage(msg);
	/*
	if (!pUnit || !pUnit->isAlive() || pUnit->GetAIInterface()->getNextTarget())
		return;

	// Switch For Emote Name (You do EmoteName to Script Name link).
	switch(Emote)
	{
	case EMOTE_ONESHOT_SALUTE: // <- Its EMOTE name.
		GuardsOnSalute(pPlayer, pUnit); // <- Its Link.
		break;

	case EMOTE_ONESHOT_KISS:
		GaurdsOnKiss(pPlayer, pUnit);
		break;
	
	case EMOTE_ONESHOT_WAVE:
		GuardsOnWave(pPlayer, pUnit);
		break;
	}
	*/
}

void SetupRandomScripts(ScriptMgr * mgr)
{	// Register Hook Event here
	Log.Notice("RandomScripts", "Setting up RandomScripts...");
	mgr->register_hook(SERVER_HOOK_EVENT_ON_EMOTE, (void *)&OnEmote);
	mgr->register_hook(SERVER_HOOK_EVENT_ON_GOSSIP_HELLO, (void *)&OnGossipHello);
	eb = new EventBridge();
}
