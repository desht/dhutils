package me.desht.dhutils.responsehandler;

public abstract class ExpectBase {

	private ResponseHandler resp;
	private String playerName;
	
	public abstract void doResponse(String playerName);

	public ResponseHandler getResp() {
		return resp;
	}

	public void setResp(ResponseHandler resp) {
		this.resp = resp;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void handleAction() {
		resp.handleAction(playerName, getClass());
	}
	
	public void cancelAction() {
		resp.cancelAction(playerName, getClass());
	}

}
