# JSON-LD analysis report
## Introduction

[Linked data](http://www.w3.org/DesignIssues/LinkedData.html) is envisioned as a way to create a network of (standardised) machine interpretable	data across different documents distributed among the Web. It provides links between pieces of data located on different computational resources, effectively allowing to start with a particular piece of data and follow its links to data hosted in different sites across the Web.
JavaScript Object Notation [(JSON)](http://www.ietf.org/rfc/rfc4627.txt) is a lightweight, text-based, language-independent data interchange format. The idea is providing an open standard for transmitting data objects consisting of attribute-value pairs. It is designed to use human-readable text for facilitating application development and debugging, and stands out as a clear alternative to XML.
### JSON-LD
JSON-LD[(http://www.w3.org/TR/json-ld/)] is designed as a method aimed to transporting linked data using JSON (JavaScript Object Notation for Linked Data) including hyper-media support and rich semantics. It tries to introduce a common language for machines talking to each other via web-services. The main objective is two-fold, express linked data in JSON and add semantics to the existing JSON standard. Just like plain JSON, the design is simple, terse and focuses on being human readable for facilitating application development and debugging. Indeed, one of the main goals when developing the JSON-LD standard was to require as little effort as possible to transform plain JSON documents to JSON-LD documents.

The idea is serialising data in a way similar to the one used in plain JSON, which provides two main benefits:
- Developer’s effort to adapt from JSON to JSON-LD is minimised
- Formats are backwards compatible, therefore applications suitable for JSON (e.g., parsers, libraries, dabatases, etc.) are suitable for JSON-LD.

In contrast to traditional semantic Web technologies (such as OWL and RDF) that tend to be triple-centric, JSON-LD takes an entity centric approach. A JSON-LD document can be represented as a linked data graph, where nodes are known as _objects_ and edges are known as _properties_. In a particular _property_ the _subject_ is the node with the outgoing edge and the _object_ the node with incoming edge. In order to make a particular _object_ or _property_ identificable and referenceable, it can be labelled with an unique IRI. This labelling will effectively support the core concept of linked data. When _objects_ are labelled with something that is not an IRI (typically, a data value such as a number, a string, etc.) they are known as _values_.

JSON-LD is structured around two main keywords _@context_ and _@id_. The _@context_ keyword is used to define the short-hand names that are used throughout a JSON-LD document. These short-hand names are called terms and help developers to express specific identifiers in a compact manner. It links _objects_ and properties in a JSON document to concepts in a particular ontology. The _@id_ keyword is used to uniquely identify things that are being described in the document with IRIs. This allows clients consuming the JSON-LD document to discover new data by simply following these links.

JSON-LD defines both expanded and [compacted](http://www.w3.org/TR/json-ld/#compacted-document-form) forms . In the expanded form the terms and prefixes are expanded into full IRIs and types and language coercions are defined in-line, so the context can be effectively removed from the document without loosing any information. In the compacted form, an user-specified context is used to generate the most compact representation of the document.
### Expanded form example
JSON-LD expanded form examples.
```
[
  {
    "@type": [
      "http://schema.org/Person"
    ],
    "http://schema.org/jobTitle": [
      {
        "@value": "Professor"
      }
    ],
    "http://schema.org/name": [
      {
        "@value": "Jane Doe"
      }
    ],
    "http://schema.org/telephone": [
      {
        "@value": "(425) 123-4567"
      }
    ],
    "http://schema.org/url": [
      {
        "@id": "http://www.janedoe.com"
      }
    ]
  }
]
```
### Compacted form example
JSON-LD compacted form example
```
{
  "@context": "http://schema.org/",
  "@type": "Person",
  "jobTitle": "Professor",
  "name": "Jane Doe",
  "telephone": "(425) 123-4567",
  "url": "http://www.janedoe.com"
}
{
  "@context": "http://schema.org/"
}
```
### JSON-LD and RDF
Please, notice transforming a JSON-LD document to RDF triples is a straightforward process. Subjects and objects are defined by the _@id_ keyword, and the other JSON-LD properties are mapped to RDF predicates. Literal values can be taken directly from a property’s value or by using the keyword _@value_ including optionally the keywords _@language_ and _@type_.

### JSON-LD and Ontologies
By using the context and the different keywords available (_e.g.,_ _@schema_, _@type_, _@id_, etc.) it is fairly simple to generate an RDF or OWL representation of the knowledge. Then, as populating the representation with data (effectively providing individuals to the taxonomy and different properties) tends to be easier in JSON than in OWL or even RDF, one can effectively refer the data structure generated in owl and populate the individuals in JSON-LD.

So far, no plugins for Protégé or another knowledge engineering tool supporting JSON-LD has been found. One possible solution is to use protégé to generate an OWL vocabulary and [generate](https://github.com/stain/owl2jsonld) a JSON-LD context from the vocabulary. Once the context has been generated, instances can be used to populate the knowledge base using JSON-LD compact form.

#### Tests with JSON-LD generation owl2jsonld
Checkout project and build
``` bash
git clone https://github.com/stain/owl2jsonld.git
cd owl2jsonld
```
Copy sample pizza OWL ontology (from the [Internet](http://130.88.198.11/co-ode-files/ontologies/pizza.owl) or from this directory) to _ owl2jsonld_ directory.
Run the converter.
``` bash
lein run -c -p -o PizzaPatatitas.context.json -P OwlOntology file:pizza.owl
```

Linked data entities are generated from the context, so individuals created in the json-ld main file can effectively reference in linked data format: 
- Ontology’s taxonomy
```
"OwlOntology:PizzaBase" : {
      "@id" : "http://www.co-ode.org/ontologies/pizza/pizza.owl#PizzaBase"
    },
```
- Ontology’s object properties
```
"OwlOntology:hasBase" : {
      "@id" : "http://www.co-ode.org/ontologies/pizza/pizza.owl#hasBase",
      "@type" : "@id"
    },
```
- Ontology’s data properties
```
"OwlOntology:hasTestDataProperty" : {
      "@id" : "http://www.co-ode.org/ontologies/pizza/pizza.owl#hasTestDataProperty",
      "@type" : "http://www.w3.org/2001/XMLSchema#double"
    },
```
Please, notice the data property has been added for testing purposes and therefore is not present on the original ontology file.

This process will effectively allow a computer program to:
- Interpret the data as plain JSON, neglecting the expressiveness of the ontology (_i.e.,_  not navigating through the linked data referring to the ontology).
- Interpret the data as an ontology, by navigating  through the linked data referring to the ontology to query the taxonomy or the meaning of the different properties
However, it comes at a cost of non being able performing the reasoning process associated to instance classification via OWL reasoners, such process must be performed manually by the programs consuming the JSON-ld data.

