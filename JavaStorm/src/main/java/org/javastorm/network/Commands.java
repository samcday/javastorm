package org.javastorm.network;

// This class is a little silly :) Primarily a convenience for adding new zackets, just gotta create
// the class, and add the name here, and voila! Also I was bored and wanted to use a few language
// constructs I don't use often (dynamic class loading, parameterized types, etc)
public enum Commands
{
	/*	ZBattleOptions,
		ZChalBattleAdd,
		ZChalBattleDel,
		ZChalBattleUpdate,
		ZChalLaunchClient,
		ZChalLoginReply,
		ZChalPlayerAdd,
		ZChalPlayerAddress,
		ZChalPlayerDel,
		ZChalPlayerUpdate,
		ZChalRequestChangeStatus,
		ZChalRequestLogin,
		ZChalRequestSailTo,
		ZChatLine,
		ZCompressedUpdateSquid,
		ZCreateCanon,
		ZDeclareDraw,
		ZFortDataPacket,
		ZInitialMoney,
		ZKeepAlive,
		ZLoadFort,
		ZLoginBegin,
		ZLoginReply,
		ZPathProcess,
		ZPlayerData,
		ZPreconnect,
		ZReadyToRock,
		ZReconnectState,
		ZRequestCleanShutdown,
		ZRequestLogin,
		ZRootQuery,
		ZRootReplyQuery,
		ZTALKBACK,
		ZTimeSync;
		
		
		private NSCommands()
		{
			try
			{
				this.commandClass = Class.forName("sambro.netstorm.network.commands." + this.name());
			}
			catch(Exception ie)
			{
				ie.printStackTrace();
				throw new RuntimeException("Sam you moron!");
			}
			
			NSCommand command = this.get();
			this.typeID = command.getCommandID();
		}
		
		public int getCommandID()
		{
			return this.typeID;
		}
		
		public NSCommand get()
		{
			try
			{
				return (NSCommand)this.commandClass.newInstance();
			} catch(Exception ie) {}
			
			return null;
		}

		private int typeID;
		private Class commandClass;*/
}
