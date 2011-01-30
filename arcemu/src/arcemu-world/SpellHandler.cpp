/*
 * ArcEmu MMORPG Server
 * Copyright (C) 2005-2007 Ascent Team <http://www.ascentemu.com/>
 * Copyright (C) 2008-2010 <http://www.ArcEmu.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#include "StdAfx.h"

void WorldSession::HandleUseItemOpcode(WorldPacket& recvPacket)
{
	CHECK_INWORLD_RETURN

	typedef std::list<Aura*> AuraList;

	Player* p_User = GetPlayer();
	sLog.outDetail("WORLD: got use Item packet, data length = %i",recvPacket.size());
	int8 tmp1,slot;
	uint8 unk; //Alice : added in 3.0.2
	uint64 item_guid;
	uint8 cn;
	uint32 spellId = 0;
	uint32 glyphIndex;
    bool found = false;

	recvPacket >> tmp1;
	recvPacket >> slot; 
	recvPacket >> cn;
	recvPacket >> spellId;
	recvPacket >> item_guid;
	recvPacket >> glyphIndex;
	recvPacket >> unk;

	Item* tmpItem = NULL;
	tmpItem = p_User->GetItemInterface()->GetInventoryItem(tmp1,slot);
	if (!tmpItem)
		tmpItem = p_User->GetItemInterface()->GetInventoryItem(slot);
	if (!tmpItem)
		return;
	ItemPrototype *itemProto = tmpItem->GetProto();

  if ( tmpItem->IsSoulbound() ){ // SouldBind item will be used after SouldBind()
    if(sScriptMgr.CallScriptedItem(tmpItem,_player))
		  return;
  }

	if(_player->getDeathState()==CORPSE)
		return;

	if(itemProto->Bonding == ITEM_BIND_ON_USE)
		tmpItem->SoulBind();

	if(sScriptMgr.CallScriptedItem(tmpItem,_player))
		return;

	if( itemProto->InventoryType != 0 && !_player->GetItemInterface()->IsEquipped(itemProto->ItemId) )//Equipable items cannot be used before they're equipped. Prevents exploits
		return;//Prevents exploits such as keeping an on-use trinket in your bag and using WPE to use it from your bag in mid-combat.

	if(itemProto->QuestId)
	{
		// Item Starter
		Quest *qst = QuestStorage.LookupEntry(itemProto->QuestId);
		if(!qst)
			return;

		WorldPacket data;
		sQuestMgr.BuildQuestDetails(&data, qst, tmpItem, 0, language, _player);
		SendPacket(&data);
	}

    // Let's check if the item even has that spell
    for( int i = 0; i < 5; ++i )
	{
        if( itemProto->Spells[i].Trigger == USE && itemProto->Spells[i].Id == spellId )
		{
            found = true;
			break;//found 1 already
		}
    }

	// Let's see if it is an onuse spellid
	if( tmpItem->HasOnUseSpellID( spellId ) )
		found = true;

    // We didn't find the spell, so the player is probably trying to cheat
    // with an edited itemcache.wdb
    //
    // Altough this could also happen after a DB update 
    // if he/she didn't delete his/her cache.
    if( found == false ){
        
        this->Disconnect();
        Anticheat_Log->writefromsession( this, "Player tried to use an item with a spell that didn't match the spell in the database." );
        Anticheat_Log->writefromsession( this, "Possibly corrupted or intentionally altered itemcache.wdb" );
        Anticheat_Log->writefromsession( this, "Itemid: %lu", itemProto->ItemId );
        Anticheat_Log->writefromsession( this, "Spellid: %lu", spellId );
        Anticheat_Log->writefromsession( this, "Player was disconnected" );

        return;
    }

	SpellCastTargets targets(recvPacket, _player->GetGUID());
	
	SpellEntry *spellInfo = dbcSpell.LookupEntryForced( spellId );
	if ( spellInfo == NULL ){
		sLog.outError("WORLD: unknown spell id %i", spellId);
		return;
	}
	
	if( spellInfo->AuraInterruptFlags & AURA_INTERRUPT_ON_STAND_UP ){
		if (p_User->CombatStatus.IsInCombat() || p_User->IsMounted()){
			_player->GetItemInterface()->BuildInventoryChangeError(tmpItem,NULL,INV_ERR_CANT_DO_IN_COMBAT);
			return;
		}
		
		if(p_User->GetStandState()!=1)
			p_User->SetStandState(STANDSTATE_SIT);
		// loop through the auras and removing existing eating spells
	}else{ // cebernic: why not stand up
		if (!p_User->CombatStatus.IsInCombat() && !p_User->IsMounted()){
			if( p_User->GetStandState() ){
				p_User->SetStandState( STANDSTATE_STAND );
			}
		}
	}
	
	// cebernic: remove stealth on using item
	if (!(spellInfo->AuraInterruptFlags & ATTRIBUTESEX_NOT_BREAK_STEALTH)){
		if( p_User->IsStealth() )
			p_User->RemoveAllAuraType( SPELL_AURA_MOD_STEALTH );
	}
	
	if(itemProto->RequiredLevel){
		if(_player->getLevel() < itemProto->RequiredLevel){
			_player->GetItemInterface()->BuildInventoryChangeError(tmpItem,NULL,INV_ERR_ITEM_RANK_NOT_ENOUGH);
			return;
		}
	}
	
	if(itemProto->RequiredSkill){
		if(!_player->_HasSkillLine(itemProto->RequiredSkill)){
			_player->GetItemInterface()->BuildInventoryChangeError(tmpItem,NULL,INV_ERR_ITEM_RANK_NOT_ENOUGH);
			return;
		}
		
		if(itemProto->RequiredSkillRank){
			if(_player->_GetSkillLineCurrent(itemProto->RequiredSkill, false) < itemProto->RequiredSkillRank){
				_player->GetItemInterface()->BuildInventoryChangeError(tmpItem,NULL,INV_ERR_ITEM_RANK_NOT_ENOUGH);
				return;
			}
		}
	}
	
	if( ( itemProto->AllowableClass && !(_player->getClassMask() & itemProto->AllowableClass) ) || ( itemProto->AllowableRace && !(_player->getRaceMask() & itemProto->AllowableRace) ) ){
		_player->GetItemInterface()->BuildInventoryChangeError(tmpItem,NULL,INV_ERR_YOU_CAN_NEVER_USE_THAT_ITEM);
		return;
	}
	
	if( !_player->Cooldown_CanCast( spellInfo ) ){
		_player->SendCastResult(spellInfo->Id, SPELL_FAILED_NOT_READY, cn, 0);
		return;
	}
	
	
	if(_player->m_currentSpell){
		_player->SendCastResult(spellInfo->Id, SPELL_FAILED_SPELL_IN_PROGRESS, cn, 0);
		return;
	}
	
	if( itemProto->ForcedPetId >= 0 ){
		if( itemProto->ForcedPetId == 0 ){
			if( _player->GetGUID() != targets.m_unitTarget ){
				_player->SendCastResult(spellInfo->Id, SPELL_FAILED_BAD_TARGETS, cn, 0);
				return;
			}
		}else{
			
			if( !_player->GetSummon() || _player->GetSummon()->GetEntry() != (uint32)itemProto->ForcedPetId ){
				_player->SendCastResult(spellInfo->Id, SPELL_FAILED_SPELL_IN_PROGRESS, cn, 0);
				return;
			}
		}
	}
	
	Spell *spell = new Spell(_player, spellInfo, false, NULL);
	spell->extra_cast_number=cn;
	spell->i_caster = tmpItem;
	spell->m_glyphslot = glyphIndex;
	
	//GetPlayer()->setCurrentSpell(spell);
	spell->prepare(&targets);

#ifdef ENABLE_ACHIEVEMENTS
	_player->GetAchievementMgr().UpdateAchievementCriteria(ACHIEVEMENT_CRITERIA_TYPE_USE_ITEM,itemProto->ItemId,0,0);
#endif

}

void WorldSession::HandleSpellClick(WorldPacket& recvPacket)
{
	CHECK_INWORLD_RETURN

	sLog.outDetail("WORLD: got CMSG_SPELLCLICK packet, data length = %i",recvPacket.size());

	if(_player->getDeathState()==CORPSE)
		return;

	uint64 target_guid; // this will store the guid of the object we are going to use it's spell. There must be a dbc that indicates what spells a unit has

	recvPacket >> target_guid;

	//we have only 1 example atm for entry : 28605
	Unit *target_unit = _player->GetMapMgr()->GetUnit( target_guid );

	if( !target_unit )
		return;

	uint32 creature_id = target_unit->GetEntry();
	uint32 cast_spell_id = 0;

	if( !_player->HasAurasWithNameHash(SPELL_HASH_LIGHTWELL_RENEW) && target_unit->RemoveAura( 59907 ) )
	{
		SpellClickSpell *sp = SpellClickSpellStorage.LookupEntry( creature_id );
		if( sp == NULL ){
			sLog.outError("Spellclick packet received for creature %u but there is no spell associated with it.", creature_id );
			return;
		}

		cast_spell_id = sp->SpellID;

		target_unit->CastSpell(_player, cast_spell_id, true);

		if( !target_unit->HasAura(59907) )
			TO_CREATURE(target_unit)->Despawn(0,0);//IsCreature() check is not needed, refer to r2387 and r3230

		return;
	}
	
	SpellClickSpell *sp = SpellClickSpellStorage.LookupEntry( creature_id );
	if( sp == NULL ){
		sLog.outError("Spellclick packet received for creature %u but there is no spell associated with it.", creature_id );
		return;
	}
	
	cast_spell_id = sp->SpellID;

	if( cast_spell_id == 0 )
		return;

	SpellEntry *spellInfo = dbcSpell.LookupEntryForced( cast_spell_id );
	if( spellInfo == NULL )
		return;
 	Spell *spell = new Spell(_player, spellInfo, false, NULL);
	SpellCastTargets targets( target_guid );
	spell->prepare(&targets);
}

void WorldSession::HandleCastSpellOpcode(WorldPacket& recvPacket)
{
	CHECK_INWORLD_RETURN

	uint32 spellId;
	uint8 cn, unk; //Alice : Added to 3.0.2

	recvPacket >> cn >> spellId  >> unk;
	// check for spell id
	SpellEntry *spellInfo = dbcSpell.LookupEntryForced(spellId );

	if(!spellInfo)
	{
		sLog.outError("WORLD: unknown spell id %i", spellId);
		return;
	}

	if( !_player->isAlive() && _player->GetShapeShift() != FORM_SPIRITOFREDEMPTION && !(spellInfo->Attributes & ATTRIBUTES_DEAD_CASTABLE))//They're dead, not in spirit of redemption and the spell can't be cast while dead.
		return;	
	
	sLog.outDetail("WORLD: got cast spell packet, spellId - %i (%s), data length = %i",
		spellId, spellInfo->Name, recvPacket.size());

	// Cheat Detection only if player and not from an item
	// this could fuck up things but meh it's needed ALOT of the newbs are using WPE now
	// WPE allows them to mod the outgoing packet and basically choose what ever spell they want :(

	if(!GetPlayer()->HasSpell(spellId))
	{
		sCheatLog.writefromsession(this,"Cast spell %lu but doesn't have that spell.", spellId);
		sLog.outDetail("WORLD: Spell isn't cast because player \'%s\' is cheating", GetPlayer()->GetName());
		return;
	}
	if(spellInfo->Attributes & ATTRIBUTES_PASSIVE)
	{
		sCheatLog.writefromsession(this,"Cast passive spell %lu.", spellId);
		sLog.outDetail("WORLD: Spell isn't cast because player \'%s\' is cheating", GetPlayer()->GetName());
		return;
	}

	if (GetPlayer()->GetOnMeleeSpell() != spellId)
	{
		//autoshot 75
		if((spellInfo->AttributesExB & ATTRIBUTESEXB_ACTIVATE_AUTO_SHOT) /*spellInfo->Attributes == 327698*/)	// auto shot..
		{
			//sLog.outString( "HandleSpellCast: Auto Shot-type spell cast (id %u, name %s)" , spellInfo->Id , spellInfo->Name );
			Item *weapon = GetPlayer()->GetItemInterface()->GetInventoryItem(EQUIPMENT_SLOT_RANGED);
			if(!weapon)
				return;
			uint32 spellid;
			switch(weapon->GetProto()->SubClass)
			{
			case 2:			 // bows
			case 3:			 // guns
            case 18:		 // crossbow
				spellid = SPELL_RANGED_GENERAL;
				break;
			case 16:			// thrown
				spellid = SPELL_RANGED_THROW;
				break;
			case 19:			// wands
				spellid = SPELL_RANGED_WAND;
				break;
			default:
				spellid = 0;
				break;
			}

			if(!spellid)
				spellid = spellInfo->Id;

			if(!_player->m_onAutoShot)
			{
				_player->m_AutoShotTarget = _player->GetSelection();
				uint32 duration = _player->GetBaseAttackTime(RANGED);
				SpellCastTargets targets(recvPacket,GetPlayer()->GetGUID());
				if(!targets.m_unitTarget)
				{
					sLog.outDebug( "Cancelling auto-shot cast because targets.m_unitTarget is null!" );
					return;
				}
				SpellEntry *sp = dbcSpell.LookupEntry(spellid);

				_player->m_AutoShotSpell = sp;
				_player->m_AutoShotDuration = duration;
				//This will fix fast clicks
				if(_player->m_AutoShotAttackTimer < 500)
					_player->m_AutoShotAttackTimer = 500;
				_player->m_onAutoShot = true;
			}

			return;
		}

        if(_player->m_currentSpell)
        {
			if( _player->m_currentSpell->getState() == SPELL_STATE_CASTING )
			{
				// cancel the existing channel spell, cast this one
				_player->m_currentSpell->cancel();
			}
			else
			{
				// send the error message
				_player->SendCastResult(spellInfo->Id, SPELL_FAILED_SPELL_IN_PROGRESS, cn, 0);
				return;
			}
        }

		SpellCastTargets targets(recvPacket,GetPlayer()->GetGUID());

		// some anticheat stuff
		if( spellInfo->self_cast_only )
		{
			if( targets.m_unitTarget && targets.m_unitTarget != _player->GetGUID() )
			{
				// send the error message
				_player->SendCastResult(spellInfo->Id, SPELL_FAILED_BAD_TARGETS, cn, 0);
				return;
			}
		}

		Spell *spell = new Spell(GetPlayer(), spellInfo, false, NULL);
		spell->extra_cast_number=cn;
		spell->prepare(&targets);
	}
}

void WorldSession::HandleCancelCastOpcode(WorldPacket& recvPacket)
{
	CHECK_INWORLD_RETURN

	uint32 spellId;
	recvPacket >> spellId;

	if(GetPlayer()->m_currentSpell)
		GetPlayer()->m_currentSpell->cancel();
}

void WorldSession::HandleCancelAuraOpcode( WorldPacket& recvPacket )
{

	CHECK_INWORLD_RETURN

	uint32 spellId;
	recvPacket >> spellId;

	if( _player->m_currentSpell && _player->m_currentSpell->GetProto()->Id == spellId )
		_player->m_currentSpell->cancel();
	else
	{
		SpellEntry *info = dbcSpell.LookupEntryForced( spellId );

		if(info != NULL && !(info->Attributes & static_cast<uint32>(ATTRIBUTES_CANT_CANCEL)))
		{
			_player->RemoveAllAuraById( spellId );
			sLog.outDebug("Removing all auras with ID: %u",spellId);
		}
	}
}

void WorldSession::HandleCancelChannellingOpcode( WorldPacket& recvPacket)
{

	CHECK_INWORLD_RETURN

	uint32 spellId;
	recvPacket >> spellId;

	Player *plyr = GetPlayer();
	if(!plyr)
		return;
	if(plyr->m_currentSpell)
	{
		plyr->m_currentSpell->cancel();
	}
}

void WorldSession::HandleCancelAutoRepeatSpellOpcode(WorldPacket& recv_data)
{
	CHECK_INWORLD_RETURN

	//sLog.outString("Received CMSG_CANCEL_AUTO_REPEAT_SPELL message.");
	//on original we automatically enter combat when creature got close to us
//	GetPlayer()->GetSession()->OutPacket(SMSG_CANCEL_COMBAT);
	GetPlayer()->m_onAutoShot = false;
}

void WorldSession::HandlePetCastSpell(WorldPacket & recvPacket)
{

	CHECK_INWORLD_RETURN

	uint64 guid;
	uint32 spellid;
	uint32 flags;
	recvPacket >> guid >> spellid >> flags;

	SpellEntry * sp = dbcSpell.LookupEntryForced(spellid);
	if ( sp == NULL )
		return;
	// Summoned Elemental's Freeze
    if (spellid == 33395)
    {
		if (!_player->GetSummon())
            return;
    }
    else if ( guid != _player->m_CurrentCharm )
    {
        return;
    }

	/* burlex: this is.. strange */
	SpellCastTargets targets;
	targets.m_targetMask = static_cast<uint16>( flags );

	if(flags == 0)
		targets.m_unitTarget = guid;
	else if(flags & TARGET_FLAG_UNIT)
	{
		WoWGuid guid2;
		recvPacket >> guid2;
		targets.m_unitTarget = guid2.GetOldGuid();
	}
	else if(flags & TARGET_FLAG_SOURCE_LOCATION)
	{
		recvPacket >> targets.m_srcX >> targets.m_srcY >> targets.m_srcZ;
	}
	else if(flags & TARGET_FLAG_DEST_LOCATION)
	{
		recvPacket >> targets.m_destX >> targets.m_destY >> targets.m_destZ;
	}
	else if (flags & TARGET_FLAG_STRING)
	{
		std::string ss;
		recvPacket >> ss;
		targets.m_strTarget = ss;
	}
	if(spellid == 33395)	// Summoned Water Elemental's freeze
	{
		Spell * pSpell = new Spell(_player->GetSummon(), sp, false, 0);
		pSpell->prepare(&targets);
	}
	else			// trinket?
	{
		Unit *nc = _player->GetMapMgr()->GetUnit( _player->m_CurrentCharm );
		if( nc )
		{
			bool check = false;
			for(list<AI_Spell*>::iterator itr = nc->GetAIInterface()->m_spells.begin(); itr != nc->GetAIInterface()->m_spells.end(); ++itr)//.......meh. this is a crappy way of doing this, I bet.
			{
				if( (*itr)->spell->Id == spellid )
				{
					check = true;
					break;
				}
			}
			if( !check )
				return;

			Spell * pSpell = new Spell(nc, sp, false, 0);
			pSpell->prepare(&targets);
		}
	}
}

void WorldSession::HandleCancelTotem(WorldPacket & recv_data)
{

	CHECK_INWORLD_RETURN

	uint8 slot;
	recv_data >> slot;

   	if( slot < 4 && _player->m_TotemSlots[slot] )
		_player->m_TotemSlots[slot]->TotemExpire();
}
