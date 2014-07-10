WebServices = angular.module("WebServices", []);

WebServices.config(function($httpProvider){
    $httpProvider.defaults.headers.get = {"Accept": "application/json"}
})

WebServices.controller("MainController", function($scope, $http){
    $scope.data = {

    }


    $http.get("datasets")
        .then(
            function(response){
                $scope.data.datasets = response.data;
            }
        )

    $scope.carregando = true
    $scope.showDataset = function(dataset){
        $http.get("dataset/" + dataset)
            .then(function(response){
                $scope.carregando = false
                $scope.data.datasetName = dataset;
                $scope.data.resource = response.data;
            }, function(response){
                $scope.carregando = false
            })
        $http.get("dataset/" + dataset + "/plot")
            .then(function(response){
                $scope.data.plotLink = response.data.url
            })
    }


})