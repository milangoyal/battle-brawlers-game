
public class Stats {

	private Integer health;
	
	private Integer attack;
	
	private Integer defense;
	
	private Integer speed;
	
	public Stats(Stats s) {
		this.health = s.health;
		this.attack = s.attack;
		this.defense = s.defense;
		this.speed = s.speed;
	}
	
	public Integer getHealth() {
	return health;
	}
	
	public void setHealth(Integer health) {
	this.health = health;
	}
	
	public Integer getAttack() {
	return attack;
	}
	
	public void setAttack(Integer attack) {
	this.attack = attack;
	}
	
	public Integer getDefense() {
	return defense;
	}
	
	public void setDefense(Integer defense) {
	this.defense = defense;
	}
	
	public Integer getSpeed() {
	return speed;
	}
	
	public void setSpeed(Integer speed) {
	this.speed = speed;
	}

}