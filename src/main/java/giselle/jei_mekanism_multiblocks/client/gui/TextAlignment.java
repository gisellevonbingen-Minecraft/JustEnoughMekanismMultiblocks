package giselle.jei_mekanism_multiblocks.client.gui;

public enum TextAlignment
{
	LEFT
		{
			@Override
			public double align(double back, double fore)
			{
				return 0.0D;
			}
		},
	CENTER
		{
			@Override
			public double align(double back, double fore)
			{
				return (back - fore) / 2.0D;
			}
		},
	RIGHT
		{
			@Override
			public double align(double back, double fore)
			{
				return back - fore;
			}
		},
	// EOL
	;

	private TextAlignment()
	{

	}

	public abstract double align(double back, double fore);

}
