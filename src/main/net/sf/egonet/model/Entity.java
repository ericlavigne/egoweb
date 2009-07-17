package net.sf.egonet.model;

public class Entity implements java.io.Serializable
{
    private Long id;

//	private static Random random;
//  private Long randomKey;

    public Long getId()
    {
        return id;
    }

	public void setId(Long id)
    {
		this.id = id;
	}

	/*
	public boolean equals(Object obj)
	{
		return obj instanceof Entity && getRandomKey().equals(((Entity) obj).getRandomKey());
	}

	public int hashCode()
	{
		return getRandomKey().hashCode();
	}

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
	*/
}
