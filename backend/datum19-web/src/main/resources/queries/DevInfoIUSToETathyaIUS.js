db.getCollection('ius').aggregate([
{
    $lookup : {
        from : 'indicator',
        localField : "Indicator_NId",
        foreignField : "Indicator_NId",
        as : "indicator"
        }
    }, {
    $lookup : {
        from : 'subgroup',
        localField : "Subgroup_Val_NId",
        foreignField : "Subgroup_Val_NId",
        as : "subgroup"
        }
    }, 
//     { $lookup : {
//          from : 'ic_ius',
//          let : {ic_nid : '$IC_NId', iusnid : '$IUSNId'},
//          pipeline : [
//             {$match : {$expr : {$eq : ['$IUSNId', '$$iusnid']}}},
//             {$lookup : {from : 'indicator_classification', localField : 'IC_NId', foreignField : 'IC_NId', as : 'ic'}},
//             {$lookup : {from : 'indicator_classification', localField : 'IC_NId', foreignField : 'IC_Parent_NId', as : 'sector'}},
//             {$unwind : {path : '$ic'}},
//             {$unwind : {path : '$sector'}},
//             {$project : {sectorname : '$ic.IC_Name', subsectorname : '$ic.IC_Name', ic_ius_nid : "$IC_IUSNId"}}
//          ],
//          as : 'theme'
//          }
//         }, 
        { $lookup : {
            from : 'unit',
            localField : 'Unit_NId',
            foreignField : 'Unit_NId',
            as : 'unit'
            }    
         },
        { $unwind : { path : "$indicator" } },
        { $unwind : { path : "$subgroup" } }, 
//         { $unwind : { path : "$theme" } },
        { $unwind : {path : "$unit"}},
        {
            $project : {
                "_id" : 0,
                "indicatorDataMap.formId" : "14",
                "indicatorDataMap.indicatorNid" : "$IUSNId",
                "indicatorDataMap.indicatorName" : {$concat : ["$indicator.Indicator_Name", "-", "$subgroup.Subgroup_Val"]},
                "indicatorDataMap.sector" : "Health",
                "indicatorDataMap.subsector" : "Health",
                "indicatorDataMap.subgroup" : "$subgroup.Subgroup_Val",
                "indicatorDataMap.highIsGood" : {$cond : {if: {$eq : ["$indicator.HighIsGood", 1]}, then : "true", else : "false"}},
                "indicatorDataMap.unit" : "$unit.Unit_Name",
                "indicatorDataMap.typeDetailId" : "",
                "_class" : "org.sdrc.datum19.document.Indicator"
                }
        },
        {
            $out : "indicator_en"
            }
])