package mikuhl.wikitools;

public class WikiToolsConfigs {
    public class WikiToolConfig {
        private String name;

        public WikiToolConfig(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    public class BooleanConfig extends WikiToolConfig {
        boolean value;

        public BooleanConfig(String name, boolean value)
        {
            super(name);
            this.value = value;
        }

        public boolean getValue()
        {
            return value;
        }
    }

    public class FloatConfig extends WikiToolConfig {
        float value;

        public FloatConfig(String name, float value)
        {
            super(name);
            this.value = value;
        }

        public float getValue()
        {
            return value;
        }
    }

    public BooleanConfig specialRotation = new BooleanConfig("Special Rotation", true);

    public FloatConfig headPitch = new FloatConfig("Head Pitch", 0.0f);
    public FloatConfig headYaw   = new FloatConfig("Head Yaw", 0.0f);
    public FloatConfig bodyYaw   = new FloatConfig("Body Yaw", 0.0f);
}
