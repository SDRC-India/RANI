db.getCollection('dataValue').aggregate([
//     {
//         $match : {
//             inid : {$in : [1]}
//             }
//         },
    {
        $lookup : {
            from : "area",
            foreignField : "areaId",
            localField : "datumId",
            as : "area"
            }
        },
        {
        $lookup : {
            from : "timePeriod",
            foreignField : "timePeriodId",
            localField : "tp",
            as : "timePeriod"
            }
        },
        {
        $lookup : {
            from : "indicator",
            foreignField : "indicatorDataMap.indicatorNid",
            localField : "inid",
            as : "indicator"
            }
        },
        {
            $unwind : {path :"$area"}
            },
            {
            $unwind : {path :"$timePeriod"}
            },
            {
            $unwind : {path :"$indicator"}
            },
        {
        $project : {
            "_id" : 0,
            "datumId" : 1,
            "dataValue" : 1,
            "tp" : 1,
            "inid" : 1,
            "indicator" : {$concat : ["$indicator.indicatorDataMap.indicatorName"," in ", "$area.areaName", " on ", "$timePeriod.timePeriodDuration"]},
            }
    },
    {$out : "dataSearch"}
])