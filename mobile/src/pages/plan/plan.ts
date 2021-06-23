import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { PlanServiceProvider } from '../../providers/plan-service/plan-service';
import { MessageServiceProvider } from '../../providers/message-service/message-service';
import { ConstantProvider } from '../../providers/constant/constant';

/**
 * Generated class for the PlanPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-plan',
  templateUrl: 'plan.html',
})
export class PlanPage {

  public tableColumn: any;
  public tableData: any;
  public heading1: any;
  public heading2: any;
  public heading3: any;
  public month: any;
  public userDisable: boolean = false;
  
  constructor(public navCtrl: NavController, public navParams: NavParams, public planProvider: PlanServiceProvider,
    public messageProvider: MessageServiceProvider) {
  }

  ionViewDidLoad() {
    this.messageProvider.showLoader(ConstantProvider.message.pleaseWait)
    this.fetchPlanData()
  }

  fetchPlanData(){
    this.planProvider.doNetworkCall().then(data=>{
      this.messageProvider.stopLoader();
      this.month = Object.keys(data)
      this.tableData = data[this.month][Object.keys(data[this.month])[1]]
      // this.tableData = []
      this.tableColumn = data[this.month][Object.keys(data[this.month])[0]]
      this.heading1 = this.tableColumn[0]
      this.heading2 = this.tableColumn[1]
      this.heading3 = this.tableColumn[2]
    }).catch(err =>{
      if(err.status == 412){
        this.messageProvider.stopLoader();
        this.messageProvider.showSuccessAlert("Error",err.error).then(data=>{
          this.navCtrl.setRoot('HomePage')
        })
      }
    });
  }
}
