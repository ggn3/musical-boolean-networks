# musical-boolean-networks
A simple sequencer/synthesisor for musical Boolean networks.

# Examples
Example network and sequence files are provided in the [examples folder](https://github.com/ggn3/musical-boolean-networks/blob/master/examples). These are plain text files written in the domain-specific language specified [here](https://github.com/ggn3/musical-boolean-networks/blob/master/examples/Domain-Specific%20Language%20Details.txt).
To run an example file, first run the Musical_Boolean_Network_Player.jar executable, then select "open" and browse to the file location. Press "reload" to parse and play the chosen file.

# Build
The JavaFX GUI application can be built using Gradle. To create an executable ".jar" file, run `gradle jfxJar`.
