import { NgModule } from '@angular/core';
import { FormSearchPipe } from './form-search/form-search';
import { HomeFormSearchPipe } from './home-form-search/home-form-search';
import { ObjIteratePipe } from './obj-iterate.pipe';
import { SortRecordPipe } from './sort-record/sort-record';
import { RemoveExtraKeysPipe } from './remove-extra-keys';

@NgModule({
	declarations: [FormSearchPipe,
    HomeFormSearchPipe,ObjIteratePipe, SortRecordPipe,RemoveExtraKeysPipe],
	imports: [],
	exports: [FormSearchPipe,
    HomeFormSearchPipe,ObjIteratePipe,SortRecordPipe,RemoveExtraKeysPipe]
})
export class PipesModule {}
