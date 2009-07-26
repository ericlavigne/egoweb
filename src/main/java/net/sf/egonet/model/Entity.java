package net.sf.egonet.model;

import java.util.Random;

public abstract class Entity implements java.io.Serializable
{
    private Long id;

	private static Random random;
	private Long randomKey;

    public Long getId()
    {
        return id;
    }

	public void setId(Long id)
    {
		this.id = id;
	}

	// ----------------------------------------

	// XXX Add citation of hibernate being fussy without these methods
	
	// pp 396-400 of Java Persistence with Hibernate discuss this issue.
	// If we avoid (sets or (detached and unsaved entities)) and cascade save 
	// then we won't need to worry about equals and hashCode. We've already
	// given up cascade save, so if we just avoid sets then we're in good shape.

	public boolean equals(Object obj)
	{
		return obj instanceof Entity && getRandomKey().equals(((Entity) obj).getRandomKey());
	}

	public int hashCode()
	{
		return getRandomKey().hashCode();
	}

	// ----------------------------------------

	protected void setRandomKey(Long randomKey) {
		this.randomKey = randomKey;
	}

	protected Long getRandomKey() {
		if(randomKey == null) {
			randomKey = generateRandom();
		}
		return randomKey;
	}

	private static Long generateRandom() {
		if(random == null) {
			random = new Random();
		}
		return random.nextLong();
	}
}
