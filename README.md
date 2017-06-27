# PROSECO
## Introduction and Usage
The PROtotype-based SErvice COmposition (PROSECO) Framework is a free and open-source Java tool for automated service composition.

PROSECO creates executable services based on *prototypes*.
The prototypes are the folders within the "prototypes" directory, which should contain "almost"-runnable software packages.
A prototype folder basically consists of (i) a folder "src" containing the actual software package and (ii) control files that determine how the grounding of that particular prototype works; a detailed description of the requirements is given below.

You run the PROSECO algorithm with a task keyword, e.g., "gender recognition", and a set of data in a format appropriate for the task (usually a .zip file). Given these two inputs, PROSECO identifies the prototype that matches the keyword best.
Ggenerally, PROSECO supports an ontological matching, but currently only an exact match is implemented; hence, there must be a folder in the "prototypes" directory that corresponds to the query keyword. PROSECO then executes the search algorithms in the "strategies" sub-directory, selects the best solution, and deploys the final service composition in a new sub-directory within your current working directory (from where you call PROSECO).

An exemplary prototype that comes with PROSECO by default is in the context of On-The-Fly Machine Learning (OTF-ML). OTF-ML aims at providing machine learning functionality to non-expert end-users. The concrete problem addressed is the one of gender recognition. The requested service is a machine learning algorithm, a classifier, that decides whether the person on a given picture (given as .jpeg or .png) is male or female. Different from AutoML approaches (e.g., AutoWEKA AutoSKLearn), PROSECO understands ML algorithms as services. In this sense, ML libraries represent repositories of such services. Using [On-The-Fly Computing](https://sfb901.uni-paderborn.de) paradigms, the computations for algorithm selection and configuration are meant to be executed in the cloud by some OTF-ML provider. Since the computations for selection and configuration might be very demanding for CPU and memory, AutoML approaches are heavily limited by the hardware specifications of the user. In contrast to this strong limitation, OTF-ML benefits from the rich hardware resources of compute centers enabling to leverage parallelization in order to speed up the overall process. 

## Setup of a Prototype Folder
Every prototype must contain (we are working on a more flexible cersion to also support non-windows systems): 
 * a file "groundingroutine.bat" replaces the placeholders of the prototype by concrete values and builds the package
 * a file "initconfiguration.bat" bootstraps the ready software package for productive usage (if necessary)
 * a folder "strategies" containing the search strategies (sub-folders) to derive concrete compositions. Each search-strategy folder must contain a file run.bat that is used by PROSECO to launch a strategy
