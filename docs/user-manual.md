## Building and Running PROSECO
In order to build PROSECO:
* clone or download the [source code](https://github.com/fmohr/PROSECO): `git clone https://github.com/fmohr/PROSECO.git`
* in the root folder run `gradle build`

A web server is contained withing PROSECO's web server submodule to provide an interface to the user. A Prototype-specific interview is presented via this web server to get necessary inputs of the prototype from the user.
 * Run `de.upb.crc901.proseco.view.app.PROSECOServer` from webserver sub-module as java application to start up the server
 * You can then access the web application via `http://localhost:8080`
 * Fill the input with `ic` for imageclassification prototype, `automl` for AutoML prototype, or `game` for cloud gaming prototype
 * You will be presented with the interview of selected prototype, fill the necessary inputs of the interview
 * At the end of the interview, best solution will be presented as result
 
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
