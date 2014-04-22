# Download the data
Download the compressed dumps of Wikipedia (in XML) in Swedish or English. I'd recommend the Swedish dump which is only 6 GB extracted compared to the English dump which is over 44 GB.

* Swedish wikipedia (676 MB):
    * http://dumps.wikimedia.org/svwiki/latest/svwiki-latest-pages-articles.xml.bz2 
* English wikipedia (10 GB):
    * http://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2

# Config

Open the file from your repo

    ./solr-4.7.2/example/example-DIH/solr/solr/conf/solr-data-config.xml
    
and change the url to your extracted wikipedia-dump:

    url="/path/to/svwiki-latest-pages-articles.xml"


# Run and import to Solr

To run Solr, go to  ```./solr-4.7.2/example/``` and run start.jar with the following command:

    java -Dsolr.solr.home="./example-DIH/solr/" -jar start.jar
    
The Solr admin can then be reached at ```http://localhost:8983/solr```.

In the left menu at the Solr admin, choose the core Solr and select [dataimport](http://localhost:8983/solr/#/solr/dataimport). You can then run the import command by pressing the execute button. The import of the Swedish dump took about 12 minutes (press Refresh Status to see progress).


# Search in Solr

To then search in the Solr admin, go to the core (left menu) and select [Query](http://localhost:8983/solr/#/solr/query).

If we for example want to search for KTH in the title, we can do the following:

    q: title:KTH

If we want to search in the text and the title:

    q: text:KTH title:KTH

Here we can read more about ranked retrieval and relevancy:

* https://wiki.apache.org/solr/SolrRelevancyFAQ