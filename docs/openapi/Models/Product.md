# Product

A product is an extended model of [**Document**](Document.md) that has on major difference: it can contain other documents that are considered variants of the major one.
However Products MUST NOT be nested, since each variant document may only contain the details that differ from the main product.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
| **id** | **String** |  | required |
| **attributes** | [**List**](Attribute.md) | multiple attributes can be delivered separately from standard data fields | [optional] [default to null] |
| **categories** | [**List<Category[]>**](Category.md) | A category path is a list of Category objects that are defined in a hierarchical parent-child relationship.Multiple category paths can be defined per document, therefor this property is a list of category arrays. | [optional] [default to null] |
| **data** | [**Map**](Document_data_value.md) | The data property should be used for standard fields, such as title, description, price. Only values of the following types are accepted (others will be dropped silently): Standard primitive types (Boolean, String, Integer, Double) and arrays of these types. Attributes (key-value objects with ID) should be passed to the attributes property. | [default to null] |
| **variants** | [**List**](Document.md) | for products without variants, it can be null or rather use a document directly. | [optional] [default to null] |

[[Back to Model list]](../index.md#documentation-for-models) [[Back to API list]](../index.md#documentation-for-api-endpoints) [[Back to README]](../index.md)

