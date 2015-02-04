package nz.dcoder.inthezone.objects;

/**
 * @author jedcalkin
 */
public class Skill {
    public String type;
    public int damage;
    public int range;
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public Skill(String type, int damage, int range){
        this.damage = damage;
    }

    public void effect(){
      // move bolder
      // create area status
      // etc.
    }
}
