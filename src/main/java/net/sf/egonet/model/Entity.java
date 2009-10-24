package net.sf.egonet.model;

import java.lang.reflect.Field;
import java.util.Random;

public abstract class Entity implements java.io.Serializable
{
    private Long id;

    private Boolean active;
    
	private static Random random;
	private Long randomKey;

	public Entity() {
		setActive(true);
	}
	
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

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getActive() {
		return active;
	}
	
	// -------------------------------------------
	
	protected String migrateToText(Entity entity, String fieldname) {
		try {
			Class<?> c = entity.getClass();
			Field oldField = c.getDeclaredField(fieldname+"Old");
			Field newField = c.getDeclaredField(fieldname);
			String oldVal = (String) oldField.get(entity);
			String newVal = (String) newField.get(entity);
			if((newVal == null || newVal.isEmpty()) && oldVal != null && ! oldVal.isEmpty()) {
				newField.set(entity, oldVal);
				oldField.set(entity, "");
			}
			return (String) newField.get(entity);
		} catch(Exception ex) {
			throw new RuntimeException("migrateToText failed",ex);
		}
	}
}
