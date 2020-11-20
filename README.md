gtfs-osm-sync [![Build Status](https://travis-ci.org/CUTR-at-USF/gtfs-osm-sync.svg?branch=master)](https://travis-ci.org/CUTR-at-USF/gtfs-osm-sync)
=============

GO_Sync is short for [General Transit Feed Specification (GTFS)](https://developers.google.com/transit/gtfs/reference) [OpenStreetMap](http://wiki.openstreetmap.org/wiki/Main_Page) Synchronization. 

It is a Java Desktop Application which can be used by a public transit agency in order to synchronize the bus stop and route information from their GTFS dataset with OpenStreetMap. This synchronization process allows an agency to upload all bus stops from their GTFS data into OpenStreetMap, as well as retrieve crowd-sourced edits such as improvements of bus stop locations or the addition of amenities such as benches, lighting, and bike racks for integration back into the transit agency's bus stop inventory. When an agency produces a new GTFS dataset with updated route and stop information, GO_Sync will automatically compare this against the contents of OSM and guide the user through merging any changes in both datasets.

The expected benefits to a transit agency and the general public are:

1. Leveraging a transit agency's GTFS bus stop data as the primary dataset for upload to OpenStreetMap, instead of requiring OSM users to code each individual bus stop
2. Retrieving OSM edits to the bus stop location and amenities back to the transit agency, so that agencies can leverage public contributions to data improvements

GO_Sync is still under development and currently has below functionalities:

1. Compare bus stops and bus routes between GTFS dataset and existing OpenStreetMap data. A report is generated for the user's convenience.
2. Upload, modify, delete bus stops from OpenStreetMap.
3. Revert a changeset in OpenStreetMap with a given changeset id.

Since two of GO_Sync's functionalities are the automated edits (e.g., upload, modify, delete, revert changeset) of OpenStreetMap and the tool is currently under development, it's highly recommended that GO_Sync users understand OpenStreetMap well and let other OpenStreetMap users know a big change is coming BEFORE any upload/revert.

## Getting Started

You'll need to install both [Git](https://git-scm.com/) for version control, and [Maven](https://maven.apache.org/index.html) for dependency management and to build the project.

Then, from the command line run:

`git clone https://github.com/CUTR-at-USF/gtfs-osm-sync.git`

`cd gtfs-osm-sync/GO_Sync`

`mvn install`

This should download the dependencies for the project (you'll need an internet connection) and create a file:

`gtfs-osm-sync\GO_Sync\target\gtfs-osm-sync-1.0.1-SNAPSHOT-jar-with-dependencies.jar`

You can double-click on this JAR file to run the application, or execute it from the command line:

`cd target`

`java -jar gtfs-osm-sync-1.0.1-SNAPSHOT-jar-with-dependencies.jar`

Note that you can also supply a list of default operators in a operators.csv file.  An example file is [here](https://github.com/CUTR-at-USF/gtfs-osm-sync/blob/master/GO_Sync/operators.csv).

Fore more info, please visit our [Getting started wiki page](https://github.com/CUTR-at-USF/gtfs-osm-sync/wiki/Getting-started) for details.

If you'd like to contribute to the project, please let us know!

## Papers and Presentations

If you're interested in the details of GO_Sync, check out our [paper](http://www.locationaware.usf.edu/wp-content/uploads/2011/10/Tran-et.-al.-GO_Sync-Framework-to-Synchronize-Crowd-sourced-Transit-Data-with-GTFS-ITS-final.pdf) and [presentation](https://www.slideshare.net/sjbarbeau/go-syncitsworldcongress20119703) from the 2011 ITS World Congress:

Khoa Tran, Ed Hillsman, Sean J. Barbeau, and Miguel Labrador. “GO! Sync – A Framework to Synchronize Crowd-Sourced Mapping Contributions From Online Communities and Transit Agency Bus Stop Inventories,” Proceedings of the 2011 ITS World Congress, Orlando, FL, October 18, 2011.

## Acknowledgements

GO_Sync was developed as part of the project ["Enabling Cost-Effective Multimodal Trip Planners through Open Transit Data."](http://www.locationaware.usf.edu/ongoing-research/open-transit-data/) (Subtask 4b: Develop protocol and software tool for data management between transit agencies and OSM). Marcy Gordon and Khoa Tran are leading the development of this software tool.

This project was funded by the Florida Department of Transportation and the National Center for Transit Research. The two principal investigators are Edward Hillsman and Sean Barbeau from the [Center for Urban Transportation Research](http://www.cutr.usf.edu/), [University of South Florida](www.usf.edu).
