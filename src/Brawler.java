import java.util.ArrayList;
import java.util.List;

public class Brawler {

	private String name;
	
	private String type;
	
	private Stats stats;
	
	private List<Ability> abilities = null;
	
	public Brawler(Brawler b) {
		this.name = b.name;
		this.type = b.type;
		this.stats = new Stats(b.stats);
		this.abilities = new ArrayList<Ability>();
		for (int i = 0; i < b.getAbilities().size(); i++) {
			Ability a = new Ability(b.getAbilities().get(i));
			abilities.add(a);
		}
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
	
	public Stats getStats() {
	return stats;
	}
	
	public void setStats(Stats stats) {
	this.stats = stats;
	}
	
	public List<Ability> getAbilities() {
	return abilities;
	}
	
	public void setAbilities(List<Ability> abilities) {
	this.abilities = abilities;
	}

}