public class Ability {

	private String name;
	
	private String type;
	
	private Integer damage;
	
	public Ability(Ability a) {
		this.name = a.name;
		this.type = a.type;
		this.damage = a.damage;
	}
	
	public String getName() {
	return name;
	}
	
	public void setName(String name) {
	this.name = name;
	}
	
	public String getType() {
	return type;
	}
	
	public void setType(String type) {
	this.type = type;
	}
	
	public Integer getDamage() {
	return damage;
	}
	
	public void setDamage(Integer damage) {
	this.damage = damage;
	}

}