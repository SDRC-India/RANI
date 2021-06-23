db.getCollection('dataValue').find({}).forEach(function(doc){
        doc.inid=String(doc.inid);
    db.dataValue.save(doc);
    })