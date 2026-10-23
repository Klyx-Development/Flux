## Flux

Flux is a powerful data-driven particle API for Paper. Effects can be defined in code via the Java API, or in JSON files, which makes this library incredibly versatile and beginner-friendly!

The library is split into a `common` module, which contains the effect/particle API, and individual platform modules that implement the library for specific server software

Currently available:
- **Paper** (`platform-paper`)

### Features
- **Shapes** - line, circle, sphere, helix, cuboid, cylinder, torus, polygon, star, heart, arc and composites of them
- **Effects** - layers of shapes with modifiers (scale, twist) and shaders (solid, gradient)
- **Particle systems** - emitters and particles with lifetime, velocity, acceleration, drag, forces, block collision, etc. (uses display entities)
- **Data files** - effects as JSON files, plus [Snowstorm](https://snowstorm.app)/bedrock particle support

### Requirements
- Java 25+
- Paper 26.2

# Installation
### Paper

In your plugin's `onEnable()` method, call `FluxPaper#init(JavaPlugin)` to initialise the library, and call `FluxPaper#destroy()` in `onDisable()`.

Do note that calling `init` will also start Exo if it hasn't already been started, so if you're also using Exo in your project, make sure to initialise it before you call `FluxPaper#init`!

```java
import org.bukkit.plugin.java.JavaPlugin;
import org.klyx.flux.paper.FluxPaper;

public class MyPlugin extends JavaPlugin {

    public void onEnable() {
        FluxPaper.init(this);
    }

    public void onDisable() {
        FluxPaper.destroy();
    }
}
```

## Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    // Common (required)
    implementation("org.klyx.flux:common:1.0.1")

    // Paper
    implementation("org.klyx.flux:paper:1.0.1")
}
```

## Maven
```xml
<!-- Common (Required) -->
<dependency>
  <groupId>org.klyx.flux</groupId>
  <artifactId>common</artifactId>
  <version>1.0.1</version>
</dependency>

<!-- Paper -->
<dependency>
  <groupId>org.klyx.flux</groupId>
  <artifactId>paper</artifactId>
  <version>1.0.1</version>
</dependency>
```

## Usage

Everything playable is a marked as a `ParticleLike`: 
- an `Effect` - shapes made with vanilla particles
- an `Emitter` (`AdvancedParticle`) - made with individual display entities. 

Examples for both of these types of particles can be found in the `demo` module and in the `examples/` folder.

### Playing a file

```java
ParticleLike particleLike = FluxPaper.blueprints().get("fire/burst");
Playback playing = FluxPaper.follow(particleLike, player, 1.0, List.of(player));

playing.pause();
playing.resume();
playing.stop();
```

Load a folder with `BlueprintRegistry#load(Path)`

### A spinning torus (Java)

```java
Effect ring = Effect.builder(
    Timeline.builder(4).loop(LoopMode.LOOP)
        .track(Tracks.YAW, Track.between(4, 0, 360, Easing.LINEAR)).build())
    .layer(Layer.of(new Torus(1.2, 0.15)).style(SamplingStyle.SURFACE).density(6)
        .shader(new GradientShader(Gradient.of(0xFF3B3B, 0x3BB8FF), GradientShader.Mode.X)))
    .build();

FluxPaper.play(ring, location, List.of(player));
```

### A spinning torus (JSON)

`plugins/[]/effects/spin.json`:

```json
{
  "duration": 4,
  "loop": "loop",
  "tracks": { "yaw": [ {"time": 0, "value": 0}, {"time": 4, "value": 360} ] },
  "layers": [ {
    "shape": {"type": "torus", "major": 2, "minor": 0.3},
    "style": "surface", "density": 4,
    "shader": {"type": "gradient", "colors": ["#FF0000", "#0000FF"], "mode": "y", "phase": "query.progress"}
  } ]
}
```

### A fountain (Java)

```java
EmitterSpec emitter = new EmitterSpec(
    new EmitterSpec.Rate.Steady(Expression.constant(60)),
    new EmitterSpec.Lifetime.Once(Expression.constant(30)),
    new EmitterShape.Disc(0.2, false),
    new Direction.Cone(new Vec3(0, 1, 0), 15),
    160
);

ParticleSpec spark = ParticleSpec.builder()
    .lifetime(Expression.parse("1.5 + variable.particle_random_1"))
    .speed(Expression.constant(6))
    .acceleration(new Vec3(0, -14, 0))
    .size(Expression.parse("0.1 * (1 - variable.particle_age / variable.particle_lifetime)"))
    .color(Gradient.of(0xFFFFFF, 0xFFD23B, 0xFF3B3B))
    .collision(Collision.bouncy(0.5))
    .build();

Playback fountain = FluxPaper.play(new AdvancedParticle(emitter, spark), location, List.of(player));
```

A system can also be registered next to the files with `FluxPaper.blueprints().register("fountain", system)`.

Both effects and advanced particles can follow an entity. Following **must** be started from the main thread!

### Bedrock and Snowstorm files

A particle file exported from Snowstorm/bedrock in general plays natively, and anything that Flux doesn't support (textures, flipbooks, events, lighting) is ignored:

```java
FluxPaper.blueprints().load(folder);
FluxPaper.play(FluxPaper.blueprints().get("rain"), location, viewers);
```

### Expressions

Numbers can be written as Molang-style expressions anywhere a blueprint or bedrock file supports one. 

Some things to note: In Molang, trigonometry is in **degrees** and `query.progress` goes from `0` to `1` over a loop, so use that instead of `query.time` to make looping effects loop perfectly

## Building

```
./gradlew build 
./gradlew :demo-paper:runServer
```

