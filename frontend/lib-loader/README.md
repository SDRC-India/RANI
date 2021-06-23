This package is developed by SDRC UI team with four optional styles of preloader. Its peer dependency is bootstrap. Please follow the installation steps.

Steps to use SDRC Table
1. Install bootstrap 


2. 
```bash
    npm i sdrc-loader
```

3. add SdrcLoaderModule to @ngModule imports
    ```js
    imports: [
        SdrcLoaderModule
    ]
    ```

Colours of loader can be overwritten in styles.css.

4. show() and hide() in SdrcLoaderService can be used to manually show or hide modal.

5. 4 types of loader :  default, ring, roller, spinner

