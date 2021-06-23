db.getCollection('_datavalue').aggregate([
{
    $project : {
        datumId : '$Area_NId',
        dataValue : '$Data_Value',
        tp : '$TimePeriod_NId',
        inid : '$IUSNId',
        _case : 'secondary_source',
        indicator_gid : "$Indicator_NId",
        datumtype : "area",
        _class : "org.sdrc.datum19.document.DataValue"
        }
    },
    {$out : 'dataValue'}
])