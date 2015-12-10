<h2>Goals</h2>
<p>
Text preprocessors has the goal of implementing algorithms that work even if they are ported and using the best statistical methods. Text mining algorithms have the detrimental trait of often being abstract and subjective. The libraries often implement certain tasks well and others without much care.
</p>
<p>
The tool aims to also make these algorithms distributable via Spark using map partitions. 
</p>
<p>
These are not <a href="http://www.simplrterms/com">SimplrTeks/SimplrTerms</a> trade secrets, just the best ways to prepare data for use in the mining process.
</p>
<h2>Implementations and Algorithms</h2>
<p>
This tool includes the following whether custom, ported or wrapped.
</p>
<p>
<ul>
  <li>Smoothing (traingular,rectangular,simple exponential with moving average,Hamming Window based; Hanning Window based)</li>
  <li>Named Entity extraction and replacement using Epic*</li>
  <li>Ported Text Segmentation from Python NLTK with more custom smoothing and a few changes *</li>
  <li>Punctuation Removal</li>
  <li>Number replacement</li>
  <li>SimplrTerms similarity based word replacement (also generates a word replacement model)*</li>
</ul>
<i>* indicates where files can be written to a non-HDFS file system (most return a RDD[String])</i>
</p>
