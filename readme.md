
# Approximation Algorithm for Constrained k-Center Clustering: A Local Search Approach

  ## Dependencies

  - Java version: 1.8.0

  ## Data sets

  - cnae (example)
  - skin 
  - covertype 

### Running the Constrained Clustering Program

To execute the algorithm:

1. **Compile the Java class**  
   ```bash
   javac addConstraints/OutPut.java


2. **Run the program**

   ```bash
   java addConstraints.OutPut
   ```

> **Note:**
> The algorithm’s clustering parameters (e.g. number of clusters, constraint ratios, repetition ratios) are currently hard-coded in `OutPut.java`. You can modify those settings directly in the source before recompiling if you need to change the configuration.

```java
// Example snippet from OutPut.java showing the settings
int[] k_vary = {10}; //number of clusters
double[] c = {0.1}; //constraint ratios
double[] c1 = {0.0}; //repetition ratios
// ...
```

The final results will be saved in `/Results/cnae/`.

  #### Plot the output
  -  Measure the clustering cost and runtime of the constrained clustering problem.
