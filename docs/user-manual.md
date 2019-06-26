## Building and Running PROSECO
In order to build PROSECO:
* clone or download the [source code](https://github.com/fmohr/PROSECO): `git clone https://github.com/fmohr/PROSECO.git`
* in the root folder run `gradlew build -x test`
* PROSECO-*.jar should be generated in the root directory run it with: `java -jar PROSECO-*.jar`
* You can then access the web application via `http://localhost:8080`

PROSECO needs prototypes to operate:
* AutoML Prototype can be set up using instructions at: https://git.cs.upb.de/SFB901-Testbed/automl-prototype
* Cloud Gaming Prototype can be set up using instructions at: https://github.com/kadirayk/game

## Setting up your IDE
### Eclipse
In the root folder run: 

```./gradlew eclipse```

This automatically creates the eclipse project files and configures the dependencies among the projects. 
Then open Eclipse and go to the import menu, e.g., in the package manager. 
Choose to import Existing Projects into Workspace, select the root folder, 
and check the **Search for nested projects** option.
 
## Creating a Prototype
In order to create a prototype domain folder structure must be followed. 
A domain can contain multiple prototypes, and each prototype can contain multiple strategies.
Each strategy must include a strategy runnable (run.bat or run.sh). Each prototype must provide a grounding and a
deployment script to handle their grounding and deployment phases. 


### Setup of a Domain Folder
    .
    ├── interview                     # Used for gathering required information to run the service
        ├── interview.yaml            # Interview process definitions
        └── questions.yaml            # Interview questions repository
    ├── prototypes
        └── prototype_name            # A domain can have multiple protoypes
            ├── strategies                # Contains search strategies to derive concrete compositions
                └── strategy_name             
                    └── run.bat               # Used for launching the strategy
            │...
            ├── deployment.bat         # deploys final configuration
            └── grounding.bat          # Replaces the placeholders of the prototype by concrete values and builds the package
			
PROSECO runs on both windows and linux systems. If PROSECO is running on windows run.bat (strategy runnable), deployment.bat, and grounding.bat must be created.
If it is running on linux run.sh (strategy runnable), deployment.sh, and grounding.sh must be created.
