import java.io.Serializable;


public class DescBean implements Serializable {
	
	private String itemId;
	private String shopId;
	private String shopName;
	private String sellerId;
	private String sellerNick;
	private String descUrl;
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getShopId() {
		return shopId;
	}
	public void setShopId(String shopId) {
		this.shopId = shopId;
	}
	public String getShopName() {
		return shopName;
	}
	public void setShopName(String shopName) {
		this.shopName = shopName;
	}
	public String getSellerId() {
		return sellerId;
	}
	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}
	public String getSellerNick() {
		return sellerNick;
	}
	public void setSellerNick(String sellerNick) {
		this.sellerNick = sellerNick;
	}
	public String getDescUrl() {
		return descUrl;
	}
	public void setDescUrl(String descUrl) {
		this.descUrl = descUrl;
	}
	@Override
	public String toString() {
		return "DescBean [itemId=" + itemId + ", shopId=" + shopId
				+ ", shopName=" + shopName + ", sellerId=" + sellerId
				+ ", sellerNick=" + sellerNick + ", descUrl=" + descUrl + "]";
	}

	
	
}
