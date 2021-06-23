import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  /**
   * Sets a key value pair in the storage
   */
  set(key: string, value: string): void{
    sessionStorage.setItem(key, value)
  }

  /**
   * Returns vaule from a key value pair by the help of key
   */
  get(key: string): string{
    return sessionStorage.getItem(key)
  }

  /**
   * Removes the key value pair from storage
   */
  remove(key: string){
    sessionStorage.removeItem(key)
  }

}
