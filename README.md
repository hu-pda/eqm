# eqm
This is an Evaluation Tool for Event Query Mining. Given a base query, that represents the situation we want to mine from historic event data traces, and one or more mined queries it will evaluate the mined queries. 

It uses the Esper EQM engine (see http://www.espertech.com/esper/) for query execution and Gnuplot (http://www.gnuplot.info/) for creating plots. 


## Usage
The proposed workflow for the usage of this tool is as follows:

1 Choose a supported data set or implement support for a new data set and create a Esper epl module containing a base query. 
2 Create historic traces from the data set by using the partitioning mode of this tool like this:

```
--partitioning=True --dataset=google-cluster --dataset-folder="/media/ntfs/eqm/DataSetFolder"
--epl-file="/media/ntfs/eqm/google_cluster_module.epl" --base-query=cluster-p1 --partitioning-window=600000 --save-dir="/media/ntfs/eqm/eqm-traces
```

3 Use your EQM implementation to create one or more esper queries you want to evaluate.
4 Use this tool for evaluation like this:

```
--dataset=google-cluster --dataset-folder="/media/ntfs/eqm/DataSetFolder" --epl-file="/media/ntfs/eqm/google_cluster_module.epl" --base-query=cluster-p1 --evaluated-queries=cluster-p1-eval,cluster-p1-eval2,cluster-p1-eval3,cluster-p2 --evaluated-groups=SVMAlgorithm,TreeAlgorithm --comparator=full --save-dir="/media/ntfs/eqm/eqm-results" --plot-dir=./resources/plots/ 
```


Note that you can create groups of queries that will be clustered in the plots. You can also implement your own gnuplot files to create custom plots.

Use option -h to get more information on the options.


