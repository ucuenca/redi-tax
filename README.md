# REDI Taxonomy

Classify publications according with the UNESCO nomenclature.

## Build
```mvn
    mvn clean install
```

## Try it

Package redi-tax.
```mvn
    mvn package appassembler:assemble
```
After you package, there are some scripts in your `${project_folder}/target/redi-tax` to populate your repository with the UNESCO nomenclature and classify a publication given some keywords.

