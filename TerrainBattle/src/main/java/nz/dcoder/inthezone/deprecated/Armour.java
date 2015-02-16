package nz.dcoder.inthezone.deprecated;
/**
 * @author jedcalkin
 */
public class Armour {
    protected double physical;
    private double antiMagic;
    
    public Armour(double physical, double antimagic){
        this.physical = physical;
        this.antiMagic = antimagic;
    }

    public double getPhysical() {
        return this.physical;
    }
    public void setPhysical(double value) {
        this.physical = value;
    }

	/**
	 * @return the antiMagic
	 */
	public double getAntiMagic() {
		return antiMagic;
	}

	/**
	 * @param antiMagic the antiMagic to set
	 */
	public void setAntiMagic(double antiMagic) {
		this.antiMagic = antiMagic;
	}
}
