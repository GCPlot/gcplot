# GCPlot - All-in-one JVM GC Logs Analyzer

GCPlot is a Java Garbage Collector (GC) logs analyzer. Basically, it's an effort to solve all GC logs reading/analyzing problems once and forever. As developers, we were tired about the current situation and efforts needed to just compare some number of GC configurations, so we decided to start from scratch and build a tool that suits best for us.

The report itself consists of a lot of graphs, measurements, stats, etc about how exactly your GC works. You can also manage the timeline and decide - whether to dig deeper, by analyzing, for example, 2 minutes interval in the most details, or check everything from the bird's eye view by choosing the last month.

# Installation

## Docker Installation

You can run GCPlot in a Docker container. Docker is supported by most of the modern OS, for more details check official [Docker Installation](https://docs.docker.com/engine/installation/) page.

In order to run GCPlot as-is without additional configuration, run next command:

`docker run -d -p 80:80 gcplot/gcplot:latest`

After that eventually the platform will be accessible from your host machine at `http://127.0.0.1` address. If you would like to use another port, just change it. For example, for `http://127.0.0.1:8080` address, the command will look like:

`docker run -d -p 8080:80 gcplot/gcplot:latest`

By default, admin user is already created, with username and password `admin`. Please consider changing it for the best security after the initial log in.
